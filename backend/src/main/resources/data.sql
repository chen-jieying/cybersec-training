INSERT INTO app_user (ID, USERNAME, PASSWORD, ROLE, FULL_NAME, GRADE, CLASS_ID, TITLE, PHONE) VALUES (1, 'student1', '123456', 'student', '张三', '初二', 1, '学生', '13800000001');
INSERT INTO app_user (ID, USERNAME, PASSWORD, ROLE, FULL_NAME, GRADE, CLASS_ID, TITLE, PHONE) VALUES (2, 'teacher1', '123456', 'teacher', '李老师', '无', NULL, '教师', '13800000002');
INSERT INTO app_user (ID, USERNAME, PASSWORD, ROLE, FULL_NAME, GRADE, CLASS_ID, TITLE, PHONE) VALUES (3, 'admin1', '123456', 'admin', '管理员', '无', NULL, '管理员', '13800000003');

INSERT INTO school_class (ID, GRADE, CLASS_NAME, TEACHER_ID, TEACHER_NAME) VALUES (1, '初二', '2班', 2, '李老师');

INSERT INTO scenario_script (ID, SCENE_TYPE, TITLE, DESCRIPTION, SOURCE) VALUES (1, '钓鱼诈骗', '校园诈骗防骗演练', '模拟假冒班主任要求转账的钓鱼诈骗场景。', '系统内置');
INSERT INTO scenario_script (ID, SCENE_TYPE, TITLE, DESCRIPTION, SOURCE) VALUES (2, '账号安全', '账号泄露演练', '模拟陌生链接获取账号密码的安全教育场景。', '系统内置');

INSERT INTO question_bank (ID, CATEGORY, QUESTION_TEXT, OPTION_A, OPTION_B, OPTION_C, OPTION_D, ANSWER, TYPE, SCORE) VALUES (1, '网络安全', '收到陌生链接时应当怎么做？', '立即点击查看', '保存备用', '先核实来源再决定', '转发给同学', 'C', 'single', 10);
INSERT INTO question_bank (ID, CATEGORY, QUESTION_TEXT, OPTION_A, OPTION_B, OPTION_C, OPTION_D, ANSWER, TYPE, SCORE) VALUES (2, '密码安全', '以下哪项密码最安全？', '123456', 'password', '网安2024！', 'abc123', 'C', 'single', 10);

INSERT INTO teaching_resource (ID, TITLE, RESOURCE_TYPE, FILE_URL, DESCRIPTION, TAGS, UPLOADED_BY) VALUES (1, '网络安全知识手册', 'pdf', '/files/security-manual.pdf', '初中生网络安全学习资料。', '网络安全,常识', 2);

INSERT INTO training_record (ID, STUDENT_ID, SCENARIO_ID, SCENARIO_TITLE, SCORE, MAX_SCORE, STATUS, COMPLETED_AT) VALUES (1, 1, 1, '校园诈骗防骗演练', 85, 100, 'completed', '2024-01-01T10:00:00');
