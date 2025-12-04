import {
    login,
  } from "@/services/api/auth/authentication.api.ts";
  import type {
    LoginForm
  } from "@/services/api/auth/authentication.api.ts";
  import { useQuery } from "@tanstack/vue-query";
  import type {UseQueryReturnType} from "@tanstack/vue-query";
  import { queryKey } from "@/constants/queryKey";
  import httpClient from '@/services/httpClient';
  
  export const useLogin = (
    params: LoginForm,
    options?: any
  ): UseQueryReturnType<Awaited<ReturnType<typeof login>>, Error> => {
    return useQuery({
      queryKey: [queryKey.authentication.login],
      queryFn: () => login(params),
      ...options,
    });
  };
  
  

