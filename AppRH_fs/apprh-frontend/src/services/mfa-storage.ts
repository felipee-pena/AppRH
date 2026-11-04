const MFA_CHALLENGE_KEY =
  "apprh.mfaChallenge";

export function saveMfaChallenge(
  challengeToken: string
): void {
  sessionStorage.setItem(
    MFA_CHALLENGE_KEY,
    challengeToken
  );
}

export function getMfaChallenge():
  string | null {
  return sessionStorage.getItem(
    MFA_CHALLENGE_KEY
  );
}

export function removeMfaChallenge():
  void {
  sessionStorage.removeItem(
    MFA_CHALLENGE_KEY
  );
}