/**
 * The properties of a row in the buckets dev-users, int-users and prd-users of the DEMO dataset - only the parts the
 * examples use. The other properties are still there; the type just describes what the tests need.
 */
export interface User {
  firstName: string;
  lastName: string;
  contact: { email: string; phone?: string };
  address: { city: string; country: string };
  /** type: standard / premium / vip, status: active / suspended / locked / closed */
  account: { type: string; status: string; verified: boolean; loyaltyPoints: number };
  auth: { login: string; password: string };
}
