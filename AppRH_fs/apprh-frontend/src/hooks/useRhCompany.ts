 import {
   useQuery,
 } from "@tanstack/react-query";

 import type {
   CompanyData,
 } from "../interfaces/CompanyData";

 import { api } from "../services/api";

 export interface RhCompanyData
   extends CompanyData {
   created: boolean;
 }

 async function fetchRhCompany():
   Promise<RhCompanyData> {
   const response =
     await api.get<RhCompanyData>(
       "/api/rh/company"
     );

   return response.data;
 }

 export function useRhCompany(
   enabled: boolean
 ) {
   return useQuery({
     queryKey: [
       "rh-company",
     ],

     queryFn: fetchRhCompany,

     enabled,

     staleTime:
       5 * 60 * 1000,

     retry: false,
   });
 }