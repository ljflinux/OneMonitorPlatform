/**
 * CMDB Store
 * 
 * Pinia store for CI (Configuration Item) state management
 */

import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { CIResponse, CIQueryParams, CILifecycleStatus } from '@/types/cmdb';
import cmdbApi from '@/api/cmdb';

export const useCmdbStore = defineStore('cmdb', () => {
  // State
  const ciList = ref<CIResponse[]>([]);
  const currentCI = ref<CIResponse | null>(null);
  const loading = ref(false);
  const error = ref<string | null>(null);
  const pagination = ref({
    page: 0,
    size: 20,
    totalElements: 0,
    totalPages: 0,
  });
  const filters = ref<CIQueryParams>({
    status: undefined,
    typeId: undefined,
    owner: undefined,
    environment: undefined,
    search: '',
  });

  // Getters
  const hasCIs = computed(() => ciList.value.length > 0);
  
  const activeCIs = computed(() => 
    ciList.value.filter(ci => ci.status === 'ACTIVE')
  );
  
  const maintenanceCIs = computed(() => 
    ciList.value.filter(ci => ci.status === 'MAINTENANCE')
  );
  
  const decommissionedCIs = computed(() => 
    ciList.value.filter(ci => ci.status === 'DECOMMISSIONED')
  );

  const ciById = computed(() => {
    return (id: string) => ciList.value.find(ci => ci.id === id);
  });

  // Actions
  async function fetchCIList(params: CIQueryParams = {}) {
    loading.value = true;
    error.value = null;

    try {
      const queryParams = {
        ...filters.value,
        ...params,
        page: params.page ?? pagination.value.page,
        size: params.size ?? pagination.value.size,
      };

      const response = await cmdbApi.list(queryParams);
      
      ciList.value = response.content;
      pagination.value = {
        page: response.pageable?.pageNumber ?? response.number ?? 0,
        size: response.size,
        totalElements: response.totalElements,
        totalPages: response.totalPages,
      };
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch CI list';
      console.error('Failed to fetch CI list:', e);
    } finally {
      loading.value = false;
    }
  }

  async function fetchCIById(id: string) {
    loading.value = true;
    error.value = null;

    try {
      const ci = await cmdbApi.getById(id);
      if (ci) {
        currentCI.value = ci;
      } else {
        error.value = 'CI not found';
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to fetch CI';
      console.error('Failed to fetch CI:', e);
    } finally {
      loading.value = false;
    }
  }

  async function createCI(command: Parameters<typeof cmdbApi.create>[0]) {
    loading.value = true;
    error.value = null;

    try {
      const newCI = await cmdbApi.create(command);
      ciList.value.unshift(newCI);
      pagination.value.totalElements++;
      return newCI;
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to create CI';
      console.error('Failed to create CI:', e);
      throw e;
    } finally {
      loading.value = false;
    }
  }

  async function updateCI(id: string, command: Parameters<typeof cmdbApi.update>[1]) {
    loading.value = true;
    error.value = null;

    try {
      const updatedCI = await cmdbApi.update(id, command);
      
      // Update in list
      const index = ciList.value.findIndex(ci => ci.id === id);
      if (index !== -1) {
        ciList.value[index] = updatedCI;
      }
      
      // Update current CI if it matches
      if (currentCI.value?.id === id) {
        currentCI.value = updatedCI;
      }
      
      return updatedCI;
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to update CI';
      console.error('Failed to update CI:', e);
      throw e;
    } finally {
      loading.value = false;
    }
  }

  async function changeStatus(id: string, status: CILifecycleStatus, reason?: string) {
    loading.value = true;
    error.value = null;

    try {
      const updatedCI = await cmdbApi.changeStatus(id, { status, reason });
      
      // Update in list
      const index = ciList.value.findIndex(ci => ci.id === id);
      if (index !== -1) {
        ciList.value[index] = updatedCI;
      }
      
      // Update current CI if it matches
      if (currentCI.value?.id === id) {
        currentCI.value = updatedCI;
      }
      
      return updatedCI;
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to change status';
      console.error('Failed to change status:', e);
      throw e;
    } finally {
      loading.value = false;
    }
  }

  async function enterMaintenance(id: string, reason: string) {
    return changeStatus(id, 'MAINTENANCE', reason);
  }

  async function activateCI(id: string, reason?: string) {
    return changeStatus(id, 'ACTIVE', reason);
  }

  async function decommission(id: string, reason: string) {
    return changeStatus(id, 'DECOMMISSIONED', reason);
  }

  async function deleteCI(id: string) {
    loading.value = true;
    error.value = null;

    try {
      await cmdbApi.delete(id);
      
      // Remove from list
      ciList.value = ciList.value.filter(ci => ci.id !== id);
      pagination.value.totalElements--;
      
      // Clear current CI if it matches
      if (currentCI.value?.id === id) {
        currentCI.value = null;
      }
    } catch (e) {
      error.value = e instanceof Error ? e.message : 'Failed to delete CI';
      console.error('Failed to delete CI:', e);
      throw e;
    } finally {
      loading.value = false;
    }
  }

  function setFilters(newFilters: Partial<CIQueryParams>) {
    filters.value = { ...filters.value, ...newFilters };
    pagination.value.page = 0; // Reset to first page
  }

  function setPage(page: number) {
    pagination.value.page = page;
  }

  function setPageSize(size: number) {
    pagination.value.size = size;
    pagination.value.page = 0;
  }

  function clearError() {
    error.value = null;
  }

  function clearCurrentCI() {
    currentCI.value = null;
  }

  return {
    // State
    ciList,
    currentCI,
    loading,
    error,
    pagination,
    filters,
    
    // Getters
    hasCIs,
    activeCIs,
    maintenanceCIs,
    decommissionedCIs,
    ciById,
    
    // Actions
    fetchCIList,
    fetchCIById,
    createCI,
    updateCI,
    changeStatus,
    enterMaintenance,
    activateCI,
    decommission,
    deleteCI,
    setFilters,
    setPage,
    setPageSize,
    clearError,
    clearCurrentCI,
  };
});
