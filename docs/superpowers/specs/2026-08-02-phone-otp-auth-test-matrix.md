# Phone + OTP Auth — Test Case Matrix

**Feature ID:** `phone-otp-auth`  
**Spec:** [`2026-08-02-phone-otp-auth-design.md`](./2026-08-02-phone-otp-auth-design.md)  
**Date:** 2026-08-02

| ID | Behavior / edge case | Status | Test(s) | Spec section |
|----|----------------------|--------|---------|--------------|
| E01 | Empty phone → Next disabled | covered | `PhoneNumberViewModelTest.emptyPhone_disablesNext` | Phone interactions |
| E02 | Invalid SG prefix → validation error | covered | `ValidatePhoneNumberUseCaseTest.sg_invalidPrefix` | Validation rules |
| E02b | Valid SG `87654321` → `PhoneNumber` | covered | `ValidatePhoneNumberUseCaseTest.sg_valid` | Validation rules |
| E03 | Country change re-validates | covered | `PhoneNumberViewModelTest.countryChange_revalidates` | Phone interactions |
| E04 | Send OTP network failure → snackbar | covered | `PhoneNumberViewModelTest.sendFailure_showsError` | Edge E04 |
| E05 | Back from OTP retains phone digits | pending | manual smoke | Grill Q4 |
| E06 | Wrong OTP → inline error, digits kept | covered | `OtpVerificationViewModelTest.wrongCode_showsError` | OTP interactions |
| E07 | Fake code `123456` → success | covered | `FakeAuthRepositoryTest.verify_validCode` | Fake repo |
| E07b | Fake non-123456 → `OtpInvalid` | covered | `FakeAuthRepositoryTest.verify_invalidCode` | Fake repo |
| E08 | Resend disabled during countdown | pending | manual smoke | OTP interactions |
| E09 | Resend issues new challengeId | covered | `OtpVerificationViewModelTest.resend_issuesNewChallengeId` | Grill Q8 |
| E10 | Session present → splash Main | covered | `SplashViewModelTest.startup_navigatesToMainWhenSessionPresent` | Splash changes |
| E11 | Onboarding flag only, no session → Auth | covered | `SplashViewModelTest.startup_navigatesToAuthWhenNoSession` | Grill Q1 |
| E12 | Verify success saves session + onboarding | covered | `OtpVerificationViewModelTest.success_savesSession` | OTP interactions |
| E13 | Mask phone `+6587654321` → `+65 •••• 4321` | covered | `PhoneNumberMaskingTest` | Masking |
| E14 | Resend cooldown from challenge timestamp | pending | manual smoke (VM uses fixed 30s ticker) | Grill Q3 |
| E15 | Double-tap Next ignored while loading | covered | `PhoneNumberViewModelTest.loading_ignoresNext` | Phone interactions |
| E16 | OTP clear (×) resets digits | covered | `OtpVerificationViewModelTest.clear_resetsDigits` | OTP interactions |
| E17 | Architecture: domain purity | covered | `./gradlew :architecture:test` | Testing |
| E18 | MY/TH/ID/PH/VN boundary digits | covered | `ValidatePhoneNumberUseCaseTest.*` | Validation rules |

**Coverage gaps:** Compose UI/screenshot tests, process-death restoration, real Ktor integration, OTP countdown timer unit tests (long-running `viewModelScope` job) — explicitly out of scope for v1.
