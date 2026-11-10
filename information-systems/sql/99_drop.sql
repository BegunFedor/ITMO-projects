-- =====================================================================
-- 99_drop.sql — полное удаление базы данных системы из текущей схемы.
-- Удаляются только объекты системы; другие таблицы схемы не затрагиваются.
-- =====================================================================
DROP VIEW IF EXISTS v_request CASCADE;

DROP TABLE IF EXISTS
    notification, meter_reading, meter, announcement_house, announcement,
    attachment, complaint, order_request, inspection_order, rating,
    request_comment, request_status_history, request, status_transition,
    request_category, inspector, staff_specialization, specialization,
    company_staff, resident_apartment, app_user, apartment, house,
    district, management_company
CASCADE;

DROP FUNCTION IF EXISTS
    create_request, suggest_executors, staff_company, register_resident, add_comment,
    auto_close_unrated, mark_overdue_orders, publish_announcement, require_role, mark_overdue_requests,
    submit_meter_reading, file_complaint, inspector_overdue_requests, issue_order,
    app_actor_id, app_comment, request_status,
    trg_request_before_insert, trg_request_before_update, trg_request_check_executor,
    trg_request_after_change, trg_request_after_insert, trg_status_before_insert, trg_status_after_insert, trg_forbid_modification, trg_rating_before_insert,
    trg_rating_after_insert, trg_reading_before_write, trg_complaint_before_insert
CASCADE;

DROP PROCEDURE IF EXISTS
    set_actor, change_status, confirm_resident, cancel_request, reject_complaint, submit_order_report, review_order,
    accept_request, assign_executor, start_work, complete_request,
    rate_request, reject_result
CASCADE;

DROP TYPE IF EXISTS
    user_role, ownership_type, request_status, resource_type,
    complaint_status, order_status
CASCADE;
