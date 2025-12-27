# Import all CRUD operations here for easier access
from app.crud.crud_user import (
    get_user,
    get_user_by_username,
    get_user_by_email,
    get_users,
    create_user,
    update_user
)
from app.crud.crud_cmdb import (
    # CI Type operations
    get_ci_type,
    get_ci_type_by_name,
    get_ci_types,
    get_ci_types_with_attributes,
    create_ci_type,
    update_ci_type,
    delete_ci_type,
    
    # CI Attribute operations
    get_ci_attribute,
    get_ci_attributes,
    create_ci_attribute,
    update_ci_attribute,
    delete_ci_attribute,
    
    # CI Data operations
    get_ci_data,
    get_ci_data_by_ci_and_attribute,
    get_ci_data_for_ci,
    create_ci_data,
    update_ci_data,
    delete_ci_data,
    
    # CI operations
    get_ci,
    get_ci_with_attributes,
    get_ci_with_relations,
    get_cis,
    create_ci,
    update_ci,
    delete_ci,
    update_ci_status,
    
    # CI Relation operations
    get_ci_relation,
    get_ci_relations,
    create_ci_relation,
    update_ci_relation,
    delete_ci_relation,
    
    # CI Change History operations
    get_ci_change_history,
    create_ci_change_history,
    
    # Helper functions
    get_ci_attributes_with_values,
    search_cis
)
