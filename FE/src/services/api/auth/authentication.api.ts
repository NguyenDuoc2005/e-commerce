import {PREFIX_API_LOGIN, PREFIX_API_LOGIN_ADMIN, PREFIX_API_REGISTER, PREFIX_API_CHANGE_PASSWORD} from "@/constants/url";
import request from "@/services/request";
import type {DefaultResponse} from "@/utils/types/api.common";
import type {AxiosResponse} from "axios";

export interface LoginForm {
    email: string;
    password: string;
}

export interface LoginResponseData {
  accessToken: string
  refreshToken: string
}

export interface RegisterForm {
    userName: string;
    email: string;
    phone: string;
    password: string;
}

export interface ChangePassWordForm {
    currentPassword: string;
    newPassword: string;
}


export const loginUser = async (
    params: LoginForm
) => {
        const res = (await request({
            url: `${PREFIX_API_LOGIN}`,
            method: "POST",
            data: params,
        })) as AxiosResponse<DefaultResponse<LoginResponseData>>;
        return res.data;  
};


export const loginAdmin = async (
    params: LoginForm
) => {
        const res = (await request({
            url: `${PREFIX_API_LOGIN_ADMIN}`,
            method: "POST",
            data: params,
        })) as AxiosResponse<DefaultResponse<LoginResponseData>>;
      return res.data; 
};

export const Register = async (
    params: RegisterForm
) => {
        const res = (await request({
            url: `${PREFIX_API_REGISTER}`,
            method: "PUT",
            data: params,
        })) as AxiosResponse<DefaultResponse<null>>;
        return res.data;
};

export const ChangePass = async (
    params: ChangePassWordForm
) => {
        const res = (await request({
            url: `${PREFIX_API_CHANGE_PASSWORD}`,
            method: "POST",
            data: params,
        })) as AxiosResponse<DefaultResponse<null>>;
        return res.data;
};
