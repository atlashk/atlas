INSERT INTO brand (id, name, created_at)
VALUES (1, 'Apple', NOW()),
       (2, 'Samsung', NOW()),
       (3, 'Sony', NOW()),
       (4, 'LG', NOW()),
       (5, 'Dell', NOW());

INSERT INTO category (id, name, created_at)
VALUES (1, 'Laptops', NOW()),
       (2, 'Smartphones', NOW()),
       (3, 'Tablets', NOW()),
       (4, 'Televisions', NOW()),
       (5, 'Accessories', NOW());

INSERT INTO product (id, name, price, quantity, status, available_from, is_active, brand_id, created_at)
VALUES ('PRD0000001', 'MacBook Pro 14"', 1999.99, 50, 'IN_STOCK', NOW(), 1, 1, NOW()),
       ('PRD0000002', 'Samsung Galaxy S23', 899.99, 100, 'IN_STOCK', NOW(), 1, 2, NOW()),
       ('PRD0000003', 'Sony WH-1000XM5', 349.99, 200, 'IN_STOCK', NOW(), 1, 3, NOW()),
       ('PRD0000004', 'LG OLED 55"', 1499.99, 0, 'DISCONTINUED', NOW(), 1, 4, NOW()),
       ('PRD0000005', 'Dell XPS 15', 1899.99, 60, 'IN_STOCK', NOW(), 1, 5, NOW()),
       ('PRD0000006', 'iPad Pro 12.9"', 1099.99, 70, 'IN_STOCK', NOW(), 1, 1, NOW()),
       ('PRD0000007', 'Samsung Galaxy Tab S8', 799.99, 80, 'IN_STOCK', NOW(), 1, 2, NOW()),
       ('PRD0000008', 'Sony Bravia 65"', 1799.99, 40, 'IN_STOCK', NOW(), 1, 3, NOW()),
       ('PRD0000009', 'LG NanoCell 75"', 1999.99, 0, 'OUT_STOCK', NOW(), 1, 4, NOW()),
       ('PRD0000010', 'Dell Inspiron 15', 849.99, 90, 'IN_STOCK', NOW(), 1, 5, NOW()),
       ('PRD0000011', 'MacBook Air 13"', 1299.99, 55, 'IN_STOCK', NOW(), 1, 1, NOW()),
       ('PRD0000012', 'Samsung Galaxy A54', 499.99, 110, 'IN_STOCK', NOW(), 1, 2, NOW()),
       ('PRD0000013', 'Sony WF-1000XM4', 279.99, 150, 'IN_STOCK', NOW(), 1, 3, NOW()),
       ('PRD0000014', 'LG UltraGear Monitor', 399.99, 100, 'IN_STOCK', NOW(), 1, 4, NOW()),
       ('PRD0000015', 'Dell Latitude 7420', 1599.99, 75, 'IN_STOCK', NOW(), 1, 5, NOW()),
       ('PRD0000016', 'Mac Studio', 3999.99, 30, 'IN_STOCK', NOW(), 1, 1, NOW()),
       ('PRD0000017', 'Samsung QLED TV', 2199.99, 0, 'OUT_STOCK', NOW(), 1, 2, NOW()),
       ('PRD0000018', 'Sony PlayStation 5', 499.99, 200, 'IN_STOCK', NOW(), 1, 3, NOW()),
       ('PRD0000019', 'LG Soundbar', 199.99, 180, 'IN_STOCK', NOW(), 1, 4, NOW()),
       ('PRD0000020', 'Alienware m15', 2099.99, 40, 'IN_STOCK', NOW(), 1, 5, NOW());

INSERT INTO product_details (product_id, description, created_at)
VALUES ('PRD0000001', 'A powerful MacBook with M2 chip and Retina display.', NOW()),
       ('PRD0000002', 'Latest Samsung flagship phone with high-end specs.', NOW()),
       ('PRD0000003', 'Noise-canceling headphones with amazing sound quality.', NOW()),
       ('PRD0000004', 'OLED Smart TV - great for gaming and streaming!', NOW()),
       ('PRD0000005', 'High-performance ultrabook with a sleek design.', NOW()),
       ('PRD0000006', 'iPad Pro 12.9" - The best tablet for professionals.', NOW()),
       ('PRD0000007', 'Premium Android tablet with a powerful chip.', NOW()),
       ('PRD0000008', 'Sony''s best Bravia model with Dolby Vision support.', NOW()),
       ('PRD0000009', 'NanoCell technology for amazing color accuracy.', NOW()),
       ('PRD0000010', 'A budget-friendly yet capable laptop.', NOW()),
       ('PRD0000011', 'Lightweight MacBook ideal for students.', NOW()),
       ('PRD0000012', 'A mid-range phone with great battery life.', NOW()),
       ('PRD0000013', 'Sony''s best-in-class wireless earbuds.', NOW()),
       ('PRD0000014', 'UltraGear gaming monitor with 240Hz refresh rate.', NOW()),
       ('PRD0000015', 'Corporate laptop optimized for security.', NOW()),
       ('PRD0000016', 'Superpower Mac Studio, perfect for professionals.', NOW()),
       ('PRD0000017', 'Samsung''s premium QLED TV with 4K HDR.', NOW()),
       ('PRD0000018', 'Next-gen gaming console with ultra-fast SSD.', NOW()),
       ('PRD0000019', 'Immersive soundbar for home theaters.', NOW()),
       ('PRD0000020', 'Powerful gaming laptop with RTX graphics.', NOW());

INSERT INTO product_attribute (id, product_id, name, value, created_at)
VALUES
    -- MacBook Pro 14"
    (1, 'PRD0000001', 'Processor', 'Apple M2', NOW()),
    (2, 'PRD0000001', 'RAM', '16GB', NOW()),
    (3, 'PRD0000001', 'Storage', '512GB SSD', NOW()),
    -- Samsung Galaxy S23
    (4, 'PRD0000002', 'Camera', '50MP Triple Lens', NOW()),
    (5, 'PRD0000002', 'Battery', '4500mAh', NOW()),
    (6, 'PRD0000002', 'Display', '6.1-inch Dynamic AMOLED', NOW()),
    -- Sony WH-1000XM5
    (7, 'PRD0000003', 'Type', 'Over-Ear', NOW()),
    (8, 'PRD0000003', 'Noise Cancelling', 'Yes', NOW()),
    (9, 'PRD0000003', 'Battery Life', '30 Hours', NOW()),
    -- LG OLED 55"
    (10, 'PRD0000004', 'Screen Size', '55 inches', NOW()),
    (11, 'PRD0000004', 'Resolution', '4K OLED', NOW()),
    (12, 'PRD0000004', 'Refresh Rate', '120Hz', NOW()),
    -- Dell XPS 15
    (13, 'PRD0000005', 'Processor', 'Intel i7-12700H', NOW()),
    (14, 'PRD0000005', 'RAM', '16GB DDR5', NOW()),
    (15, 'PRD0000005', 'Storage', '1TB SSD', NOW()),
    -- iPad Pro 12.9"
    (16, 'PRD0000006', 'Display', '12.9-inch Liquid Retina XDR', NOW()),
    (17, 'PRD0000006', 'Chip', 'Apple M1', NOW()),
    (18, 'PRD0000006', 'Storage', '512GB', NOW()),
    -- Samsung Galaxy Tab S8
    (19, 'PRD0000007', 'Battery Life', '12 Hours', NOW()),
    (20, 'PRD0000007', 'Storage', '256GB', NOW()),
    (21, 'PRD0000007', 'Display', '11-inch LCD', NOW()),
    -- Sony Bravia 65"
    (22, 'PRD0000008', 'Screen Size', '65 inches', NOW()),
    (23, 'PRD0000008', 'HDR', 'Dolby Vision', NOW()),
    (24, 'PRD0000008', 'Refresh Rate', '120Hz', NOW()),
    -- LG NanoCell 75"
    (25, 'PRD0000009', 'Screen Size', '75 inches', NOW()),
    (26, 'PRD0000009', 'Technology', 'NanoCell', NOW()),
    (27, 'PRD0000009', 'Resolution', '4K UHD', NOW()),
    -- Dell Inspiron 15
    (28, 'PRD0000010', 'Processor', 'Intel i5-1135G7', NOW()),
    (29, 'PRD0000010', 'RAM', '8GB DDR4', NOW()),
    (30, 'PRD0000010', 'Storage', '512GB SSD', NOW()),
    -- MacBook Air 13"
    (31, 'PRD0000011', 'Processor', 'Apple M2', NOW()),
    (32, 'PRD0000011', 'RAM', '8GB', NOW()),
    (33, 'PRD0000011', 'Storage', '256GB SSD', NOW()),
    -- Samsung Galaxy A54
    (34, 'PRD0000012', 'Camera', '48MP Quad', NOW()),
    (35, 'PRD0000012', 'Battery', '5000mAh', NOW()),
    (36, 'PRD0000012', 'Display', '6.4-inch AMOLED', NOW()),
    -- Sony WF-1000XM4
    (37, 'PRD0000013', 'Type', 'In-Ear', NOW()),
    (38, 'PRD0000013', 'Noise Cancelling', 'Yes', NOW()),
    (39, 'PRD0000013', 'Battery Life', '24 Hours', NOW()),
    -- LG UltraGear Monitor
    (40, 'PRD0000014', 'Screen Size', '27 inches', NOW()),
    (41, 'PRD0000014', 'Refresh Rate', '165Hz', NOW()),
    (42, 'PRD0000014', 'Resolution', '2560 x 1440', NOW()),
    -- Dell Latitude 7420
    (43, 'PRD0000015', 'Processor', 'Intel i7-1185G7', NOW()),
    (44, 'PRD0000015', 'RAM', '16GB LPDDR4X', NOW()),
    (45, 'PRD0000015', 'Storage', '512GB SSD', NOW()),
    -- Mac Studio
    (46, 'PRD0000016', 'Processor', 'Apple M1 Ultra', NOW()),
    (47, 'PRD0000016', 'RAM', '64GB Unified Memory', NOW()),
    (48, 'PRD0000016', 'Storage', '1TB SSD', NOW()),
    -- Samsung QLED TV
    (49, 'PRD0000017', 'Screen Size', '65 inches', NOW()),
    (50, 'PRD0000017', 'Technology', 'QLED', NOW()),
    (51, 'PRD0000017', 'Resolution', '4K UHD', NOW()),
    -- Sony PlayStation 5
    (52, 'PRD0000018', 'Processor', 'Custom AMD Ryzen Zen 2', NOW()),
    (53, 'PRD0000018', 'GPU', 'RDNA 2-based GPU', NOW()),
    (54, 'PRD0000018', 'Storage', '825GB SSD', NOW()),
    -- LG Soundbar
    (55, 'PRD0000019', 'Channels', '5.1.2', NOW()),
    (56, 'PRD0000019', 'Power Output', '500W', NOW()),
    (57, 'PRD0000019', 'Wireless Subwoofer', 'Yes', NOW()),
    -- Alienware m15
    (58, 'PRD0000020', 'Processor', 'Intel i9-12900H', NOW()),
    (59, 'PRD0000020', 'GPU', 'NVIDIA RTX 3080', NOW()),
    (60, 'PRD0000020', 'Storage', '2TB SSD', NOW());

INSERT INTO product_category (product_id, category_id, created_at)
VALUES
    -- Laptops
    ('PRD0000001', 1, NOW()),
    -- MacBook Pro 14" → Laptops
    ('PRD0000005', 1, NOW()),
    -- Dell XPS 15 → Laptops
    ('PRD0000010', 1, NOW()),
    -- Dell Inspiron 15 → Laptops
    ('PRD0000011', 1, NOW()),
    -- MacBook Air 13" → Laptops
    ('PRD0000015', 1, NOW()),
    -- Dell Latitude 7420 → Laptops
    ('PRD0000016', 1, NOW()),
    -- Mac Studio → Laptops
    ('PRD0000020', 1, NOW()),
    -- Alienware m15 → Laptops, also an accessory
    -- Smartphones
    ('PRD0000002', 2, NOW()),
    -- Samsung Galaxy S23 → Smartphones
    ('PRD0000012', 2, NOW()),
    -- Samsung Galaxy A54 → Smartphones
    -- Tablets (Sometimes used as Accessories or for Productivity)
    ('PRD0000006', 3, NOW()),
    -- iPad Pro 12.9" → Tablets
    ('PRD0000007', 3, NOW()),
    -- Samsung Galaxy Tab S8 → Tablets
    ('PRD0000006', 5, NOW()),
    -- iPad Pro 12.9" → Accessories (Used with Keyboard Case)
    ('PRD0000007', 5, NOW()),
    -- Galaxy Tab S8 → Accessories (Stylus, Keyboard Cases)
    -- Televisions (Also used for Gaming & Streaming)
    ('PRD0000004', 4, NOW()),
    -- LG OLED 55" → Televisions
    ('PRD0000008', 4, NOW()),
    -- Sony Bravia 65" → Televisions
    ('PRD0000009', 4, NOW()),
    -- LG NanoCell 75" → Televisions
    ('PRD0000017', 4, NOW()),
    -- Samsung QLED TV → Televisions
    ('PRD0000004', 5, NOW()),
    -- LG OLED 55" → Accessories (Used with Wall Mounts & Audio Systems)
    ('PRD0000017', 5, NOW()),
    -- Samsung QLED TV → Accessories (Wall Mounts & Soundbars)
    -- Gaming Consoles (Belongs to Televisions & Accessories)
    ('PRD0000018', 4, NOW()),
    -- PlayStation 5 → Televisions (for Gaming)
    ('PRD0000018', 5, NOW()),
    -- PlayStation 5 → Accessories (Extra Controllers, VR Headsets)
    -- Accessories (Standalone electronics)
    ('PRD0000003', 5, NOW()),
    -- Sony WH-1000XM5 → Accessories (Headphones)
    ('PRD0000013', 5, NOW()),
    -- Sony WF-1000XM4 → Accessories (Wireless Earbuds)
    ('PRD0000014', 5, NOW()),
    -- LG UltraGear Monitor → Accessories (Gaming Monitor)
    ('PRD0000019', 5, NOW()),
    -- LG Soundbar → Accessories (Audio Equipment)
    ('PRD0000020', 5, NOW());
