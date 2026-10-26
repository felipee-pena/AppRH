import type {
  ReactNode,
} from "react";

import {
  QueryClient,
  QueryClientProvider,
} from "@tanstack/react-query";

export function createQueryWrapper() {
  const queryClient =
    new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
          gcTime: 0,
        },

        mutations: {
          retry: false,
          gcTime: 0,
        },
      },
    });

  function QueryWrapper({
    children,
  }: {
    children: ReactNode;
  }) {
    return (
      <QueryClientProvider
        client={queryClient}
      >
        {children}
      </QueryClientProvider>
    );
  }

  return {
    queryClient,
    QueryWrapper,
  };
}