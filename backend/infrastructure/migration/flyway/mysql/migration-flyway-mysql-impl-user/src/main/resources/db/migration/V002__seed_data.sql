INSERT INTO users (id, username, password, first_name, last_name, email, phone_number, role, created_at)
VALUES (1, 'admin', '$2a$12$JBXIjeVKldJZ0824t5ULHOLeoq330xmpx0Ua/5Ipz4hlGxlSm9nE2', 'Atlas',
        'Admin', '0nfyGkH+0gr94SirOesbVBiKm53ZvmKJ6eHb6S4Rkykgs2u2hlsW9SL0/g==',
        'IztcSvy+JXBxWWk3Q+1RIrAW8JOqPqVa0HePA+UPxYS0FgL9Yq4=', 'ADMIN', NOW()),
       (2, 'user', '$2a$12$JBXIjeVKldJZ0824t5ULHOLeoq330xmpx0Ua/5Ipz4hlGxlSm9nE2', 'John', 'Doe',
        '8vheIMl1kmFVlPzc9NDbpocdNNroW7BZZOzB/mla3ku3vSaseCg7mtwA',
        'e5k0u/kv8e5KgiWN50y9+x1MYIIvT6h9JtWl5+b7o8j3Yuf8Bwg=', 'USER', NOW());
