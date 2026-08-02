# cmp-template

Kotlin Multiplatform app template with clean architecture (domain, data, shared presentation).

## Language

**Authentication**:
The state where a user has verified their phone number and holds a valid session (access + refresh tokens).
_Avoid_: Login, signed in (use only in UI copy when needed)

**Session**:
The persisted credential pair that proves the user is authenticated. Its presence is the sole gate for entering the main app after splash.
_Avoid_: Token, user session object (in domain speech)

**Onboarding (legacy)**:
A first-run flag stored locally from the retired carousel feature tour. Still written when authentication completes, but not used for routing decisions.
_Avoid_: First-run, welcome flow (when meaning the old carousel)

**Phone verification**:
The two-step flow of entering a mobile number and confirming a one-time code sent to that number.
_Avoid_: OTP flow (acceptable in technical docs), PIN entry

**Challenge**:
A server-issued identifier tying an OTP send attempt to a later verify or resend request.
_Avoid_: Request ID, transaction ID

**Verification code**:
The numeric one-time code sent to the user's phone number, entered on the OTP screen to complete authentication.
_Avoid_: PIN, passcode (reserved for future wallet/PIN flows)

**Masked phone number**:
A display-safe form of the user's phone number shown on the OTP screen, hiding middle digits so the number is recognizable but not fully exposed. Formatted client-side as `{dialCode} •••• {last4}`.
_Avoid_: Redacted number, blurred number (UI implementation terms)

**Masked phone number**:
A privacy-safe display of the destination phone number on the OTP screen, hiding most digits while keeping country code and last digits visible.
_Avoid_: Redacted number, blurred phone
