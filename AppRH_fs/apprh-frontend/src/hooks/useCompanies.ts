 import {
   useQuery,
 } from "@tanstack/react-query";

 import type {
   CompanyData,
 } from "../interfaces/CompanyData";

 import { api } from "../services/api";

 async function fetchCompanies():
   Promise<CompanyData[]> {
   const response =
     await api.get<CompanyData[]>(
       "/api/companies"
     );

   return Array.isArray(response.data)
     ? response.data
     : [];
 }

 export function useCompanies() {
   return useQuery({
     queryKey: ["companies"],
     queryFn: fetchCompanies,
     staleTime: 5 * 60 * 1000,
     retry: 1,
   });
 }