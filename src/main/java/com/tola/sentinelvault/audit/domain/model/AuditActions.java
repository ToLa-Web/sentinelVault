package com.tola.sentinelvault.audit.domain.model;

public final class AuditActions {

    private AuditActions() {}

    public static final String LOGIN_SUCCESS        = "identity.login.success";
    public static final String LOGIN_FAILURE        = "identity.login.failure";
    public static final String LOGOUT               = "identity.logout";
    public static final String REGISTER_SUCCESS     = "identity.register.success";
    public static final String REGISTER_FAILURE     = "identity.register.failure";
    public static final String TOKEN_REFRESH_OK     = "identity.token.refresh.success";
    public static final String TOKEN_REFRESH_FAIL   = "identity.token.refresh.failure";
    public static final String ROLE_UPDATED         = "identity.role.updated";

    public static final String VAULT_SECRET_CREATED = "vault.secret.created";
    public static final String VAULT_SECRET_UPDATED = "vault.secret.updated";
    public static final String VAULT_SECRET_DELETED = "vault.secret.deleted";
}