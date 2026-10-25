export interface ApiErrorResponse {
    status: number;
    message: string;
    errors?: Record<string, string>;
}