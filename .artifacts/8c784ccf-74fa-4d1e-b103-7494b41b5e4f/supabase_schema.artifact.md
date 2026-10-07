# Supabase SQL Schema for WellMe Merchant B2C Loans & Till Tracking

Run the following SQL script in your **Supabase SQL Editor** to create the tables required for tracking till balances, merchant loan requests, and B2C disbursements via Safaricom Daraja Sandbox API.

```sql
-- 1. Create central community till / pool table to track incoming student deposits and balance
CREATE TABLE IF NOT EXISTS public.community_till_balance (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    till_number TEXT NOT NULL UNIQUE,
    balance_in_cents BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Insert default sandbox till record
INSERT INTO public.community_till_balance (till_number, balance_in_cents)
VALUES ('174379', 100000000) -- 1,000,000 KSh initial pool liquidity for sandbox testing
ON CONFLICT (till_number) DO NOTHING;

-- 2. Create disbursed_merchant_loans table to track all merchant capital requests and B2C disbursements
CREATE TABLE IF NOT EXISTS public.disbursed_merchant_loans (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    merchant_id UUID NOT NULL,
    business_name TEXT NOT NULL,
    amount_requested_in_cents BIGINT NOT NULL,
    discount_percentage NUMERIC(5,2) NOT NULL,
    till_number TEXT NOT NULL DEFAULT '174379',
    b2c_conversation_id TEXT,
    b2c_transaction_id TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING', -- PENDING, DISBURSED, FAILED, REPAID
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Enable Row Level Security (RLS)
ALTER TABLE public.community_till_balance ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.disbursed_merchant_loans ENABLE ROW LEVEL SECURITY;

-- Allow public read/write access for sandbox testing
CREATE POLICY "Allow all access on community_till_balance"
    ON public.community_till_balance
    FOR ALL
    USING (true)
    WITH CHECK (true);

CREATE POLICY "Allow all access on disbursed_merchant_loans"
    ON public.disbursed_merchant_loans
    FOR ALL
    USING (true)
    WITH CHECK (true);

-- 3. Stored Procedure for B2C Loan Disbursement with Till Liquidity Check
CREATE OR REPLACE FUNCTION public.request_till_b2c_loan(
    p_merchant_id UUID,
    p_business_name TEXT,
    p_amount_requested_in_cents BIGINT,
    p_discount_percentage NUMERIC(5,2),
    p_till_number TEXT DEFAULT '174379'
)
RETURNS JSONB
LANGUAGE plpgsql
AS $$
DECLARE
    v_till_balance BIGINT;
    v_loan_id UUID;
BEGIN
    -- Check till liquidity balance
    SELECT balance_in_cents INTO v_till_balance
    FROM public.community_till_balance
    WHERE till_number = p_till_number;

    IF v_till_balance IS NULL THEN
        RETURN jsonb_build_object('success', false, 'message', 'Till number not found in community pool.');
    END IF;

    IF v_till_balance < p_amount_requested_in_cents THEN
        RETURN jsonb_build_object(
            'success', false,
            'message', format('Insufficient till liquidity. Available: KSh %s, Requested: KSh %s', v_till_balance / 100, p_amount_requested_in_cents / 100)
        );
    END IF;

    -- Deduct from till liquidity balance
    UPDATE public.community_till_balance
    SET balance_in_cents = balance_in_cents - p_amount_requested_in_cents,
        updated_at = NOW()
    WHERE till_number = p_till_number;

    -- Insert loan request record
    INSERT INTO public.disbursed_merchant_loans (
        merchant_id,
        business_name,
        amount_requested_in_cents,
        discount_percentage,
        till_number,
        status
    )
    VALUES (
        p_merchant_id,
        p_business_name,
        p_amount_requested_in_cents,
        p_discount_percentage,
        p_till_number,
        'DISBURSED'
    )
    RETURNING id INTO v_loan_id;

    RETURN jsonb_build_object(
        'success', true,
        'loan_id', v_loan_id,
        'message', 'Loan successfully approved and queued for Safaricom Daraja B2C disbursement.'
    );
END;
$$;
```
