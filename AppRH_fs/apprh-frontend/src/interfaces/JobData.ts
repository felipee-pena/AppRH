import type {
  CompanyData,
} from "./CompanyData";

export interface JobData {
  code?: number;
  name: string;
  description: string;
  expirationDate: string;
  createdAt?: string;
  salary: number;
  contactEmail?: string | null;
  company?: CompanyData;
}

export interface JobRequestData {
  name: string;
  description: string;
  expirationDate: string;
  salary: number;
  contactEmail?: string | null;
  companyId?: number | null;
}