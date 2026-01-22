/**
 * CMDB API Service
 * 
 * API client for CI (Configuration Item) management endpoints
 */

import type {
  CIResponse,
  CICreateCommand,
  CIUpdateCommand,
  CIQueryParams,
  PaginatedResponse,
  StatusChangeCommand,
} from '@/types/cmdb';
import axiosInstance from '@/utils/axios';

const API_BASE = '/api/v1/cmdb/cis';

export const cmdbApi = {
  /**
   * Creates a new CI
   */
  async create(command: CICreateCommand): Promise<CIResponse> {
    const response = await axiosInstance.post<CIResponse>(API_BASE, command);
    return response.data;
  },

  /**
   * Gets a CI by ID
   */
  async getById(id: string): Promise<CIResponse | null> {
    try {
      const response = await axiosInstance.get<CIResponse>(`${API_BASE}/${id}`);
      return response.data;
    } catch {
      return null;
    }
  },

  /**
   * Updates an existing CI
   */
  async update(id: string, command: CIUpdateCommand): Promise<CIResponse> {
    const response = await axiosInstance.put<CIResponse>(`${API_BASE}/${id}`, command);
    return response.data;
  },

  /**
   * Changes the status of a CI
   */
  async changeStatus(id: string, command: StatusChangeCommand): Promise<CIResponse> {
    const params = new URLSearchParams({
      status: command.status,
      ...(command.reason && { reason: command.reason }),
    });
    const response = await axiosInstance.patch<CIResponse>(
      `${API_BASE}/${id}/status?${params}`
    );
    return response.data;
  },

  /**
   * Puts a CI into maintenance mode
   */
  async enterMaintenance(id: string, reason: string): Promise<CIResponse> {
    const response = await axiosInstance.post<CIResponse>(
      `${API_BASE}/${id}/maintenance?reason=${encodeURIComponent(reason)}`
    );
    return response.data;
  },

  /**
   * Returns a CI to active status
   */
  async activate(id: string, reason?: string): Promise<CIResponse> {
    const params = reason ? `?reason=${encodeURIComponent(reason)}` : '';
    const response = await axiosInstance.post<CIResponse>(`${API_BASE}/${id}/activate${params}`);
    return response.data;
  },

  /**
   * Decommission a CI
   */
  async decommission(id: string, reason: string): Promise<CIResponse> {
    const response = await axiosInstance.post<CIResponse>(
      `${API_BASE}/${id}/decommission?reason=${encodeURIComponent(reason)}`
    );
    return response.data;
  },

  /**
   * Gets all CIs with filtering and pagination
   */
  async list(params: CIQueryParams = {}): Promise<PaginatedResponse<CIResponse>> {
    const queryParams = new URLSearchParams();
    
    if (params.typeId) queryParams.set('typeId', params.typeId);
    if (params.status) queryParams.set('status', params.status);
    if (params.owner) queryParams.set('owner', params.owner);
    if (params.environment) queryParams.set('environment', params.environment);
    if (params.search) queryParams.set('search', params.search);
    if (params.page !== undefined) queryParams.set('page', String(params.page));
    if (params.size !== undefined) queryParams.set('size', String(params.size));
    if (params.sortBy) queryParams.set('sortBy', params.sortBy);
    if (params.sortDirection) queryParams.set('sortDirection', params.sortDirection);

    const response = await axiosInstance.get<PaginatedResponse<CIResponse>>(
      `${API_BASE}?${queryParams.toString()}`
    );
    return response.data;
  },

  /**
   * Gets multiple CIs by IDs
   */
  async getByIds(ids: string[]): Promise<CIResponse[]> {
    const response = await axiosInstance.post<CIResponse[]>(`${API_BASE}/batch`, ids);
    return response.data;
  },

  /**
   * Gets correlation labels for a CI
   */
  async getCorrelationLabels(id: string): Promise<Record<string, string>> {
    const response = await axiosInstance.get<Record<string, string>>(
      `${API_BASE}/${id}/labels`
    );
    return response.data;
  },

  /**
   * Deletes a CI
   */
  async delete(id: string): Promise<void> {
    await axiosInstance.delete(`${API_BASE}/${id}`);
  },
};

export default cmdbApi;
