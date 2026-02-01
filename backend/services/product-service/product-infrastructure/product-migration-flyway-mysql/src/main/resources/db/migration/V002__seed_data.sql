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
VALUES (1, 'MacBook Pro 14"', 1999.99, 50, 'IN_STOCK', NOW(), 1, 1, NOW()),
       (2, 'Samsung Galaxy S23', 899.99, 100, 'IN_STOCK', NOW(), 1, 2, NOW()),
       (3, 'Sony WH-1000XM5', 349.99, 200, 'IN_STOCK', NOW(), 1, 3, NOW()),
       (4, 'LG OLED 55"', 1499.99, 0, 'DISCONTINUED', NOW(), 1, 4, NOW()),
       (5, 'Dell XPS 15', 1899.99, 60, 'IN_STOCK', NOW(), 1, 5, NOW()),
       (6, 'iPad Pro 12.9"', 1099.99, 70, 'IN_STOCK', NOW(), 1, 1, NOW()),
       (7, 'Samsung Galaxy Tab S8', 799.99, 80, 'IN_STOCK', NOW(), 1, 2, NOW()),
       (8, 'Sony Bravia 65"', 1799.99, 40, 'IN_STOCK', NOW(), 1, 3, NOW()),
       (9, 'LG NanoCell 75"', 1999.99, 0, 'OUT_STOCK', NOW(), 1, 4, NOW()),
       (10, 'Dell Inspiron 15', 849.99, 90, 'IN_STOCK', NOW(), 1, 5, NOW()),
       (11, 'MacBook Air 13"', 1299.99, 55, 'IN_STOCK', NOW(), 1, 1, NOW()),
       (12, 'Samsung Galaxy A54', 499.99, 110, 'IN_STOCK', NOW(), 1, 2, NOW()),
       (13, 'Sony WF-1000XM4', 279.99, 150, 'IN_STOCK', NOW(), 1, 3, NOW()),
       (14, 'LG UltraGear Monitor', 399.99, 100, 'IN_STOCK', NOW(), 1, 4, NOW()),
       (15, 'Dell Latitude 7420', 1599.99, 75, 'IN_STOCK', NOW(), 1, 5, NOW()),
       (16, 'Mac Studio', 3999.99, 30, 'IN_STOCK', NOW(), 1, 1, NOW()),
       (17, 'Samsung QLED TV', 2199.99, 0, 'OUT_STOCK', NOW(), 1, 2, NOW()),
       (18, 'Sony PlayStation 5', 499.99, 200, 'IN_STOCK', NOW(), 1, 3, NOW()),
       (19, 'LG Soundbar', 199.99, 180, 'IN_STOCK', NOW(), 1, 4, NOW()),
       (20, 'Alienware m15', 2099.99, 40, 'IN_STOCK', NOW(), 1, 5, NOW());

INSERT INTO product_details (product_id, description, created_at)
VALUES (1, 'A powerful MacBook with M2 chip and Retina display.', NOW()),
       (2, 'Latest Samsung flagship phone with high-end specs.', NOW()),
       (3, 'Noise-canceling headphones with amazing sound quality.', NOW()),
       (4, 'OLED Smart TV - great for gaming and streaming!', NOW()),
       (5, 'High-performance ultrabook with a sleek design.', NOW()),
       (6, 'iPad Pro 12.9" - The best tablet for professionals.', NOW()),
       (7, 'Premium Android tablet with a powerful chip.', NOW()),
       (8, 'Sony''s best Bravia model with Dolby Vision support.', NOW()),
       (9, 'NanoCell technology for amazing color accuracy.', NOW()),
       (10, 'A budget-friendly yet capable laptop.', NOW()),
       (11, 'Lightweight MacBook ideal for students.', NOW()),
       (12, 'A mid-range phone with great battery life.', NOW()),
       (13, 'Sony''s best-in-class wireless earbuds.', NOW()),
       (14, 'UltraGear gaming monitor with 240Hz refresh rate.', NOW()),
       (15, 'Corporate laptop optimized for security.', NOW()),
       (16, 'Superpower Mac Studio, perfect for professionals.', NOW()),
       (17, 'Samsung''s premium QLED TV with 4K HDR.', NOW()),
       (18, 'Next-gen gaming console with ultra-fast SSD.', NOW()),
       (19, 'Immersive soundbar for home theaters.', NOW()),
       (20, 'Powerful gaming laptop with RTX graphics.', NOW());

INSERT INTO product_attribute (id, product_id, name, value, created_at)
VALUES
    -- MacBook Pro 14"
    (1, 1, 'Processor', 'Apple M2', NOW()),
    (2, 1, 'RAM', '16GB', NOW()),
    (3, 1, 'Storage', '512GB SSD', NOW()),
    -- Samsung Galaxy S23
    (4, 2, 'Camera', '50MP Triple Lens', NOW()),
    (5, 2, 'Battery', '4500mAh', NOW()),
    (6, 2, 'Display', '6.1-inch Dynamic AMOLED', NOW()),
    -- Sony WH-1000XM5
    (7, 3, 'Type', 'Over-Ear', NOW()),
    (8, 3, 'Noise Cancelling', 'Yes', NOW()),
    (9, 3, 'Battery Life', '30 Hours', NOW()),
    -- LG OLED 55"
    (10, 4, 'Screen Size', '55 inches', NOW()),
    (11, 4, 'Resolution', '4K OLED', NOW()),
    (12, 4, 'Refresh Rate', '120Hz', NOW()),
    -- Dell XPS 15
    (13, 5, 'Processor', 'Intel i7-12700H', NOW()),
    (14, 5, 'RAM', '16GB DDR5', NOW()),
    (15, 5, 'Storage', '1TB SSD', NOW()),
    -- iPad Pro 12.9"
    (16, 6, 'Display', '12.9-inch Liquid Retina XDR', NOW()),
    (17, 6, 'Chip', 'Apple M1', NOW()),
    (18, 6, 'Storage', '512GB', NOW()),
    -- Samsung Galaxy Tab S8
    (19, 7, 'Battery Life', '12 Hours', NOW()),
    (20, 7, 'Storage', '256GB', NOW()),
    (21, 7, 'Display', '11-inch LCD', NOW()),
    -- Sony Bravia 65"
    (22, 8, 'Screen Size', '65 inches', NOW()),
    (23, 8, 'HDR', 'Dolby Vision', NOW()),
    (24, 8, 'Refresh Rate', '120Hz', NOW()),
    -- LG NanoCell 75"
    (25, 9, 'Screen Size', '75 inches', NOW()),
    (26, 9, 'Technology', 'NanoCell', NOW()),
    (27, 9, 'Resolution', '4K UHD', NOW()),
    -- Dell Inspiron 15
    (28, 10, 'Processor', 'Intel i5-1135G7', NOW()),
    (29, 10, 'RAM', '8GB DDR4', NOW()),
    (30, 10, 'Storage', '512GB SSD', NOW()),
    -- MacBook Air 13"
    (31, 11, 'Processor', 'Apple M2', NOW()),
    (32, 11, 'RAM', '8GB', NOW()),
    (33, 11, 'Storage', '256GB SSD', NOW()),
    -- Samsung Galaxy A54
    (34, 12, 'Camera', '48MP Quad', NOW()),
    (35, 12, 'Battery', '5000mAh', NOW()),
    (36, 12, 'Display', '6.4-inch AMOLED', NOW()),
    -- Sony WF-1000XM4
    (37, 13, 'Type', 'In-Ear', NOW()),
    (38, 13, 'Noise Cancelling', 'Yes', NOW()),
    (39, 13, 'Battery Life', '24 Hours', NOW()),
    -- LG UltraGear Monitor
    (40, 14, 'Screen Size', '27 inches', NOW()),
    (41, 14, 'Refresh Rate', '165Hz', NOW()),
    (42, 14, 'Resolution', '2560 x 1440', NOW()),
    -- Dell Latitude 7420
    (43, 15, 'Processor', 'Intel i7-1185G7', NOW()),
    (44, 15, 'RAM', '16GB LPDDR4X', NOW()),
    (45, 15, 'Storage', '512GB SSD', NOW()),
    -- Mac Studio
    (46, 16, 'Processor', 'Apple M1 Ultra', NOW()),
    (47, 16, 'RAM', '64GB Unified Memory', NOW()),
    (48, 16, 'Storage', '1TB SSD', NOW()),
    -- Samsung QLED TV
    (49, 17, 'Screen Size', '65 inches', NOW()),
    (50, 17, 'Technology', 'QLED', NOW()),
    (51, 17, 'Resolution', '4K UHD', NOW()),
    -- Sony PlayStation 5
    (52, 18, 'Processor', 'Custom AMD Ryzen Zen 2', NOW()),
    (53, 18, 'GPU', 'RDNA 2-based GPU', NOW()),
    (54, 18, 'Storage', '825GB SSD', NOW()),
    -- LG Soundbar
    (55, 19, 'Channels', '5.1.2', NOW()),
    (56, 19, 'Power Output', '500W', NOW()),
    (57, 19, 'Wireless Subwoofer', 'Yes', NOW()),
    -- Alienware m15
    (58, 20, 'Processor', 'Intel i9-12900H', NOW()),
    (59, 20, 'GPU', 'NVIDIA RTX 3080', NOW()),
    (60, 20, 'Storage', '2TB SSD', NOW());

INSERT INTO product_category (product_id, category_id, created_at)
VALUES
    -- Laptops
    (1, 1, NOW()),
    -- MacBook Pro 14" → Laptops
    (5, 1, NOW()),
    -- Dell XPS 15 → Laptops
    (10, 1, NOW()),
    -- Dell Inspiron 15 → Laptops
    (11, 1, NOW()),
    -- MacBook Air 13" → Laptops
    (15, 1, NOW()),
    -- Dell Latitude 7420 → Laptops
    (16, 1, NOW()),
    -- Mac Studio → Laptops
    (20, 1, NOW()),
    -- Alienware m15 → Laptops, also an accessory
    -- Smartphones
    (2, 2, NOW()),
    -- Samsung Galaxy S23 → Smartphones
    (12, 2, NOW()),
    -- Samsung Galaxy A54 → Smartphones
    -- Tablets (Sometimes used as Accessories or for Productivity)
    (6, 3, NOW()),
    -- iPad Pro 12.9" → Tablets
    (7, 3, NOW()),
    -- Samsung Galaxy Tab S8 → Tablets
    (6, 5, NOW()),
    -- iPad Pro 12.9" → Accessories (Used with Keyboard Case)
    (7, 5, NOW()),
    -- Galaxy Tab S8 → Accessories (Stylus, Keyboard Cases)
    -- Televisions (Also used for Gaming & Streaming)
    (4, 4, NOW()),
    -- LG OLED 55" → Televisions
    (8, 4, NOW()),
    -- Sony Bravia 65" → Televisions
    (9, 4, NOW()),
    -- LG NanoCell 75" → Televisions
    (17, 4, NOW()),
    -- Samsung QLED TV → Televisions
    (4, 5, NOW()),
    -- LG OLED 55" → Accessories (Used with Wall Mounts & Audio Systems)
    (17, 5, NOW()),
    -- Samsung QLED TV → Accessories (Wall Mounts & Soundbars)
    -- Gaming Consoles (Belongs to Televisions & Accessories)
    (18, 4, NOW()),
    -- PlayStation 5 → Televisions (for Gaming)
    (18, 5, NOW()),
    -- PlayStation 5 → Accessories (Extra Controllers, VR Headsets)
    -- Accessories (Standalone electronics)
    (3, 5, NOW()),
    -- Sony WH-1000XM5 → Accessories (Headphones)
    (13, 5, NOW()),
    -- Sony WF-1000XM4 → Accessories (Wireless Earbuds)
    (14, 5, NOW()),
    -- LG UltraGear Monitor → Accessories (Gaming Monitor)
    (19, 5, NOW()),
    -- LG Soundbar → Accessories (Audio Equipment)
    (20, 5, NOW());
