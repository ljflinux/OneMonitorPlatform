/**
 * CMDB API Type Definitions
 * 
 * Type definitions for CI (Configuration Item) management
 */

// CI Lifecycle Status
export type CILifecycleStatus = 
  | 'ACTIVE' 
  | 'MAINTENANCE' 
  | 'DECOMMISSIONED' 
  | 'DISPOSED';

// CI Response DTO
export interface CIResponse {
  id: string;
  typeId: string;
  name: string;
  displayName: string;
  status: CILifecycleStatus;
  statusDisplayName: string;
  attributes: Record<string, unknown>;
  labels: Record<string, string>;
  owner: string | null;
  department: string | null;
  location: string | null;
  environment: string | null;
  description: string | null;
  createdBy: string | null;
  updatedBy: string | null;
  createdAt: string;
  updatedAt: string;
}

// CI Create Command
export interface CICreateCommand {
  typeId: string;
  name: string;
  displayName?: string;
  attributes: Record<string, unknown>;
  labels?: Record<string, string>;
  owner?: string;
  department?: string;
  location?: string;
  environment?: string;
  description?: string;
  createdBy?: string;
}

// CI Update Command
export interface CIUpdateCommand {
  attributes?: Record<string, unknown>;
  labels?: Record<string, string>;
  owner?: string;
  displayName?: string;
  department?: string;
  location?: string;
  environment?: string;
  description?: string;
  updatedBy?: string;
}

// CI Query Parameters
export interface CIQueryParams {
  typeId?: string;
  status?: CILifecycleStatus;
  owner?: string;
  environment?: string;
  search?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'asc' | 'desc';
}

// Paginated Response
export interface PaginatedResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

// CI Relation Types
export type CIRelationType = 
  | 'CONTAINS' 
  | 'DEPENDS_ON' 
  | 'RUNS_ON' 
  | 'CONNECTED_TO' 
  | 'MANAGES' 
  | 'PART_OF' 
  | 'REPLICATES_TO';

// CI Relation Response
export interface CIRelationResponse {
  id: string;
  sourceCiId: string;
  targetCiId: string;
  type: CIRelationType;
  typeDisplayName: string;
  validFrom: string;
  validTo: string | null;
  attributes: Record<string, unknown>;
  description: string | null;
  createdBy: string | null;
}

// Status Change Command
export interface StatusChangeCommand {
  status: CILifecycleStatus;
  reason?: string;
}

// API Error Response
export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
}

// Batch Request
export interface BatchRequest<T> {
  items: T[];
}
