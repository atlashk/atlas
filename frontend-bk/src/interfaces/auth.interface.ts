export interface LoginRequest {
    identifier: string;
    password: string;
}

export interface LoginResponse {
    accessToken: string;
    refreshToken: string;
}
