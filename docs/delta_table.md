# Web → Android parity delta table

Source mapping: `docs/ui_components.json` and current Android implementation.

| Web feature / route | Android status | Notes | Priority | Estimated complexity |
| --- | --- | --- | --- | --- |
| Login (`/login`) | ✅ Implemented | Phone/password login normalization aligned to `@loanapp.local`. | Critical | Low |
| Customer dashboard (`/`) | ✅ Implemented | Scoped customer dashboard now shows live totals and counts. | Critical | Medium |
| Admin dashboard (`/`) | ✅ Implemented | Admin stats now show live counts. | High | Medium |
| Customers list (`/customers`) | ⚠️ Partial | Search + list exists; missing inline edit/actions from web. | High | Medium |
| Customer detail (`/customers/:id`) | ✅ Implemented | Android screen now wired to customer, loan, subscription, and entry data. | Critical | High |
| Add customer (`/` admin-only) | ✅ Implemented | AddCustomerScreen exists. | High | Low |
| Loan list (`/loans`) | ⚠️ Partial | List + add loan implemented; missing sorting/filtering. | Critical | Medium |
| Loan detail (`/loans/:id`) | ✅ Implemented | Installment recording now wired. | Critical | Medium |
| Loan seniority queue (`/loan-seniority`) | ✅ Implemented | UI screen + add/remove flow. | High | Medium |
| Subscriptions list (`/subscriptions`) | ⚠️ Partial | List + add + soft delete exists; edit/update missing. | Critical | Medium |
| Summary (`/summary`) | ✅ Implemented | Summary screen exists with charts. | High | Medium |
| Data entries (`/data`) | ✅ Implemented | List + add + edit + delete. | High | Medium |
| Add record (`/add-record`) | ⚠️ Partial | Action list exists; no form wizard (relies on add flows elsewhere). | Medium | Medium |
| Trash (`/trash`) | ❌ Missing | No trash UI for restore/permanent delete. | Low | High |
| Change password modal | ❌ Missing | No change password dialog or flow. | Medium | Medium |
| Inactivity logout modal | ✅ Implemented (dialog) | Dialog exists but not wired to inactivity tracking. | Medium | Medium |
| Delete confirmation modal | ✅ Implemented | Dialog exists. | High | Low |
| Record loan modal | ✅ Implemented | Bottom sheet exists. | High | Low |
| Record installment modal | ✅ Implemented | UI wired to repository and sync. | Critical | Medium |
| Record subscription modal | ✅ Implemented | Bottom sheet exists. | High | Low |
| Record data entry modal | ✅ Implemented | Bottom sheet exists. | High | Low |
| Customer detail modal | ✅ Implemented | Full-screen detail wired to data. | Critical | High |
| FY breakdown modal | ❌ Missing | Not implemented in Android summary. | Low | Medium |
