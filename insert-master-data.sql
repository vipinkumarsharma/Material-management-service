-- ====================================================================
-- Master Data Insert Script for Material Management System
-- Beejapuri Dairy Pvt Ltd
-- ====================================================================

-- ====================================================================
-- 1. BRANCH MASTER (7 branches)
-- ====================================================================

INSERT INTO branch_master (branch_id, branch_name, address_1, gst_no, pincode) VALUES ('TKRIGGN', 'Beejapuri Dairy Pvt Ltd-Tikri', 'Plot No- 15/1/2/2, Village - Tikri , Sec -48 ,Gurgaon-122001', '06AAFCB1753M1ZL', '122001');

INSERT INTO branch_master (branch_id, branch_name, address_1, gst_no, pincode) VALUES ('KHTLGGN', 'Beejapuri Dairy Pvt Ltd-Khtl', 'Warehouse Near By JBM Company,Village Khatola Sector-74,Gurgaon(HR)', '06AAFCB1753M1ZL', '122004');

INSERT INTO branch_master (branch_id, branch_name, address_1, gst_no, pincode) VALUES ('JHAJRGGN', 'Beejapuri Dairy Pvt Ltd-Jhjr', 'Khewat / Khata no. 297/345, Mustil / Killa No. 3//8/1 (3-7), 13/2 (4-4), 18/1 (7-7), 22/2 (0-2), 23 (8-0) Village Nangla, Tehsil Badli, District Jhajjar', '06AAFCB1753M1ZL', '124103');

INSERT INTO branch_master (branch_id, branch_name, address_1, gst_no, pincode) VALUES ('PUNE', 'Beejapuri Dairy Pvt Ltd-Pune', 'Gate No 302,Vill : Kharabwadi, Tal. Khed, Chakan Industrial Area, Pune', '27AAFCB1753M1ZH', '410501');

INSERT INTO branch_master (branch_id, branch_name, address_1, gst_no, pincode) VALUES ('CHITTOOR', 'Beejapuri Dairy Pvt Ltd-Chittr', '502-3A 502-2 502-1A 502-3 Gandharamakulapalle vill. Kongatam post, Venkatagirikota Mandal, chittoor-District(Andhra Pradesh)', '37AAFCB1753M1ZG', '517424');

INSERT INTO branch_master (branch_id, branch_name, address_1, gst_no, pincode) VALUES ('HYDERABAD', 'Beejapuri Dairy Pvt Ltd-Hyd', 'Sy.No.339, IDA, PH-V , Chinnakanjarla(Village), Patancheru(Mandal), Sangareddy District – 502319 Telangana', '36AAFCB1753M1ZI', '502319');

INSERT INTO branch_master (branch_id, branch_name, address_1, gst_no, pincode) VALUES ('KOLKATA', 'Beejapuri Dairy Pvt Ltd-Klkta', 'Plot No. 36,60 and 61, Mouza belumilki, JI No. 11, PS-Serampore, Belumilki Hooghly, West Bengal', '19AAFCB1753M1ZE', '712223');


-- ====================================================================
-- 2. GROUP MASTER (19 product groups)
-- ====================================================================

INSERT INTO group_master (group_id, group_desc) VALUES ('PBAG', 'Packaging Bags');

INSERT INTO group_master (group_id, group_desc) VALUES ('BREADPOLY', 'Bread Poly');

INSERT INTO group_master (group_id, group_desc) VALUES ('BROUCHERS', 'Marketing Brochure');

INSERT INTO group_master (group_id, group_desc) VALUES ('CORRBOX', 'Corrugated Boxes');

INSERT INTO group_master (group_id, group_desc) VALUES ('CUP', 'Cups');

INSERT INTO group_master (group_id, group_desc) VALUES ('FOIL', 'Foilcups');

INSERT INTO group_master (group_id, group_desc) VALUES ('GLASSJAR', 'Glass Jars Bottles');

INSERT INTO group_master (group_id, group_desc) VALUES ('INGREDNT', 'Ingrediants');

INSERT INTO group_master (group_id, group_desc) VALUES ('JARLID', 'Jars Lids');

INSERT INTO group_master (group_id, group_desc) VALUES ('CUPLID', 'Cup Lids');

INSERT INTO group_master (group_id, group_desc) VALUES ('MCARTN', 'Mono Cartons');

INSERT INTO group_master (group_id, group_desc) VALUES ('PANEERPCH', 'Paneer Pouches');

INSERT INTO group_master (group_id, group_desc) VALUES ('PLSTICBOX', 'Plastic Boxes');

INSERT INTO group_master (group_id, group_desc) VALUES ('POLYFILM', 'Poly Films');

INSERT INTO group_master (group_id, group_desc) VALUES ('POUCHES', 'Pouches All');

INSERT INTO group_master (group_id, group_desc) VALUES ('SLEEVES', 'Sleeves');

INSERT INTO group_master (group_id, group_desc) VALUES ('TETRAPK', 'Tetra Packages');

INSERT INTO group_master (group_id, group_desc) VALUES ('TAPES', 'Tissue Tape Double Sides');

INSERT INTO group_master (group_id, group_desc) VALUES ('STICKER', 'Stickers');


-- ====================================================================
-- 3. UNIT MASTER (Units of Measurement)
-- ====================================================================

INSERT INTO unit_master (unit_id, unit_desc) VALUES ('Kgs', 'Kilograms');

INSERT INTO unit_master (unit_id, unit_desc) VALUES ('Nos', 'Numbers/Pieces');

INSERT INTO unit_master (unit_id, unit_desc) VALUES ('Ltr', 'Litres');

INSERT INTO unit_master (unit_id, unit_desc) VALUES ('Pack', 'Packet');


-- ====================================================================
-- 4. ITEM MASTER (Products - 133 items)
-- ====================================================================
-- Table structure: item_id, item_desc, group_id, sub_group_id, manuf_id, unit_id, gst_perc, cost_price, mrp, hsn_code, cess_perc

INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111001', 'POLYFILM COW MILK 500ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111002', 'POLYFILM COW MILK 450ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111003', 'POLYFILM BUFFALO MILK 500 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111004', 'POLYFILM BUFFALO MILK 450 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111005', 'POLYFILM LOW FAT COW MILK 500 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111006', 'POLYFILM LOW FAT COW MILK 450 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111007', 'POLYFILM LOW FAT DAHI-400 GM', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111008', 'POLYFILM LOW FAT CURD 400 GM', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111009', 'POLYFILM LOW FAT BM CURD 400 GM', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111010', 'POLYFILM CREAMY COW MILK 450 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111011', 'POLYFILM A2 COW MILK 500 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111012', 'POLYFILM LOW MALAI BM 450 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111013', 'POLYFILM LOW MALAL TONED BM 450 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111014', 'POLYFILM LOW CREAM BM 450 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111015', 'POLYFILM LOW CREAM BM 400 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111016', 'POLYFILM COW MILK 500 ML ( TAMIL)', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111017', 'POLYFILM COW MILK 450ML (TAMIL)', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111018', 'POLYFILM BM 500 ML ( TAMIL)', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111019', 'POLYFILM BM 450 ML (TAMIL)', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111020', 'POLYFILM CM 500 ML (KARNATAKA)', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111021', 'POLYFILM CM 450 ML (KARNATAKA)', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111022', 'POLYFILM BM 500 ML (KARNATAKA)', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111023', 'POLYFILM BM 450 ML (KARNATAKA)', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111024', 'POLYFILM COW MILK 400 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111025', 'POLYFILM BUFFALO MILK 400 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111026', 'POLYFILM CREAMY COW MILK 400 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111027', 'POLYFILM LOW FAT COW MILK 400 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111028', 'POLYFILM LOW MALAI BM 400 ML', 'POLYFILM', 'Kgs', 18, 157.00, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD129001', 'LP A2 1000ML', 'TETRAPK', 'Nos', 5, 6.65, '48192020');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD122001', 'PANEER POUCH 180 GM', 'PANEERPCH', 'Nos', 18, 1.10, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD122002', 'PANEER POUCH 200 GM (HIGH PROTEIN)', 'PANEERPCH', 'Nos', 18, 1.10, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD122003', 'PANEER POUCH 180 GM HIGH PROTIEN', 'PANEERPCH', 'Nos', 18, 1.10, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD116001', 'DAHI CUP LIDS 400 ML', 'CUPLID', 'Nos', 18, 0.72, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115001', 'DAHI CUP 400 ML', 'CUP', 'Nos', 18, 3.41, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115002', 'DAHI CUP BM 400 ML', 'CUP', 'Nos', 18, 3.41, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115003', 'HIGH PROTEIN DAHI CUP 400 GMS', 'CUP', 'Nos', 18, 3.44, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115004', 'SALTED BUTTER DAHI CUP 85 GM', 'CUP', 'Nos', 18, 1.68, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115005', 'UNSALTED BUTTER DAHI CUP 85 GM', 'CUP', 'Nos', 18, 1.68, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115006', 'CURD CUPS 400 GM (ENGLISH)', 'CUP', 'Nos', 18, 3.41, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115007', 'CURD CUP 400 GM (TAMIL)', 'CUP', 'Nos', 18, 3.41, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115008', 'Jeera Chaach Cups (250GM)', 'CUP', 'Nos', 18, 2.52, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115009', 'Pudina Masala Cups (250GM)', 'CUP', 'Nos', 18, 2.52, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115010', 'MANGO CUP 85ML (YOGURT)', 'CUP', 'Nos', 18, 1.68, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115011', 'STRAWBERRY CUP 85ML (YOGURT)', 'CUP', 'Nos', 18, 1.68, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115012', 'BLUEBERRY CUP 85ML (YOGURT)', 'CUP', 'Nos', 18, 1.68, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115013', 'PANJABI LASSI CUP 140ML', 'CUP', 'Nos', 18, 2.57, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115014', 'MANGO LASSI CUP 140ML', 'CUP', 'Nos', 18, 2.14, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115015', '160ML MANGO BADAM (YOGURT CUP)', 'CUP', 'Nos', 18, 2.51, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115016', '160ML STRAWBERRY BADAM (YOGURT CUP)', 'CUP', 'Nos', 18, 2.51, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115017', '160ML VANILLA BADAM (YOGURT CUP)', 'CUP', 'Nos', 18, 2.51, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115018', 'COCONUT WATER CUP 160ML', 'CUP', 'Nos', 18, 2.57, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115019', 'SUGERCAN JUICE CUP (250GMS)', 'CUP', 'Nos', 18, 1.97, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115020', '1KG CLEAR CAP JAR PLASTIC', 'CUP', 'Nos', 18, 11.6, '392330');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115021', '600ML CLEAR CAP JAR PLASTIC', 'CUP', 'Nos', 18, 11.6, '392330');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115022', 'GREEK BLUEBERRY YOGURT CUP 100ML', 'CUP', 'Nos', 18, 1.67, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115023', 'GREEK MANGO YOGURT CUP 100ML', 'CUP', 'Nos', 18, 1.67, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115024', 'GREEK NATURAL YOGURT CUP 100ML', 'CUP', 'Nos', 18, 1.67, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('DB115025', 'GREEK VANILLA YOGURT CUP 100ML', 'CUP', 'Nos', 18, 1.67, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD113001', 'Marketing Brochure', 'BROUCHERS', 'Nos', 5, 3.85, '490110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD113002', 'Milk Lab Report - 8 Pages', 'BROUCHERS', 'Nos', 5, 3.65, '490110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD123001', 'BORI', 'PBAG', 'Nos', 5, 25, '490110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD123002', 'Milk Testing Kit Packing Bag (Small', 'PBAG', 'Nos', 18, 1.2, '39232100');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD123003', 'CREAM LINER BAG', 'PBAG', 'Kgs', 18, 122, '39231010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117001', 'Cow Ghee Aluminimum Foil', 'FOIL', 'Nos', 18, 2.5, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117002', 'CURD FOILS 400 GM (ENGLISH)', 'FOIL', 'Nos', 18, 0.65, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117003', 'CURD FOILS 400 GM (TAMIL)', 'FOIL', 'Nos', 18, 0.65, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117004', 'CURD FOIL HIGH PROTEIN 400 GM', 'FOIL', 'Nos', 18, 0.65, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117005', 'GREEK VANILLA YOUGHURT FOIL', 'FOIL', 'Nos', 18, 0.48, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117006', 'Jeera Chaach foils', 'FOIL', 'Nos', 18, 0.55, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117007', 'Pudina Masala Foils', 'FOIL', 'Nos', 18, 0.55, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117008', 'BLUEBERRY CUP FOIL (YOGURT)', 'FOIL', 'Nos', 18, 0.48, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117009', 'STRAWBERRY CUP FOIL (YOGURT)', 'FOIL', 'Nos', 18, 0.48, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117010', 'MANGO CUP FOIL (YOGURT)', 'FOIL', 'Nos', 18, 0.48, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117011', 'PANJABI LASSI CUP FOIL 65MM', 'FOIL', 'Nos', 18, 0.48, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117012', 'MANGO LASSI CUP FOIL 65MM', 'FOIL', 'Nos', 18, 0.48, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117013', 'VANILLA BADAM FOIL', 'FOIL', 'Nos', 18, 0.48, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117014', 'MANGO BADAM FOIL', 'FOIL', 'Nos', 18, 0.48, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117015', 'STRAWBERRY BADAM FOIL', 'FOIL', 'Nos', 18, 0.48, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117016', 'COCONUT WATER CUP FOIL 65MM', 'FOIL', 'Nos', 18, 0.48, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117017', 'SUGER CANE JUICE FOIL 80MM', 'FOIL', 'Nos', 18, 0.65, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117018', 'HIGH PROTEIN DAHI  ALUMINIUM FOIL', 'FOIL', 'Nos', 18, 0.56, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD117019', 'CURD FOIL 400 GM', 'FOIL', 'Nos', 18, 0.56, '76071999');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111043', 'Poly Film - Low Fat Dahi Roll(400ml', 'POLYFILM', 'Kgs', 18, 157, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111044', 'POLY FILM A-2 Cow milk', 'POLYFILM', 'Kgs', 18, 157, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111045', 'POLYFILM NATURAL COW MILK 425ML', 'POLYFILM', 'Kgs', 18, 157, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111046', 'POLYFILM NATURAL COW MILK 450ML', 'POLYFILM', 'Kgs', 18, 157, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111047', 'POLYFILM NATURAL HIGH PROTEINCM 450ML', 'POLYFILM', 'Kgs', 18, 157, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111048', 'POLYFILM NATURAL HIGH PROTEIN BM 450ML', 'POLYFILM', 'Kgs', 18, 157, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD112001', 'White Bread 400GM', 'BREADPOLY', 'Kgs', 18, 210, '392329');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD112002', 'Brown Bread 400GM', 'BREADPOLY', 'Kgs', 18, 210, '392329');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD112003', 'Multigrain Bread 400GM', 'BREADPOLY', 'Kgs', 18, 210, '392329');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD112004', 'Whole Wheat Bread 400GM', 'BREADPOLY', 'Kgs', 18, 210, '392329');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD112005', 'KULCHA POUCH 200GM', 'BREADPOLY', 'Kgs', 18, 205, '392329');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD112006', 'WHOLE WHEAT PAV POUCH 300GM', 'BREADPOLY', 'Kgs', 18, 210, '392329');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111029', 'PP LAMINATED ROLL 150MM', 'POLYFILM', 'Kgs', 18, 160, '39206290');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111030', 'PP LAMINATED ROLL 160MM', 'POLYFILM', 'Kgs', 18, 160, '39206290');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD124001', '250gm Sweet Box', 'PLSTICBOX', 'Nos', 18, 1.6, '392310');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD124002', '250gm Sweet Box Lid', 'PLSTICBOX', 'Nos', 18, 1.2, '39231010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD124003', '300ml RC Food Packaging Cont', 'PLSTICBOX', 'Nos', 18, 1.4, '39231010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD124004', '300 ML Sweets Box Leed Round', 'PLSTICBOX', 'Nos', 18, 1.2, '39231010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD121001', 'BUTTER CHOCO CHIPS BOX 300GM', 'MCARTN', 'Nos', 5, 6.95, '19054000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD121002', 'BUTTER ATA BISCUIT BOX 300GM', 'MCARTN', 'Nos', 5, 6.95, '19054000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD121003', 'BUTTER KAJU PISTA COOKIES BOX 300GM', 'MCARTN', 'Nos', 5, 6.95, '19054000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD121004', 'BUTTER OSMANIA COOKIES BOX 300GM', 'MCARTN', 'Nos', 5, 6.95, '19054000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD112007', 'BROWN BREAD POUCH 200GM', 'BREADPOLY', 'Kgs', 18, 210, '392329');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD112008', 'WHITE BREAD POUCH 200GM', 'BREADPOLY', 'Kgs', 18, 210, '392329');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD112009', 'MULTIGRAIN BREAD POUCH 200GM', 'BREADPOLY', 'Kgs', 18, 210, '392329');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119001', 'DOUBLE REFINED SUGER', 'INGREDNT', 'Kgs', 5, 45.71, '17019990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126001', 'BREAD SLEEVES (ZERO MAIDA)', 'SLEEVES', 'Nos', 5, 2.75, '490110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126002', 'BREAD SLEEVES (HIGH PROTIN)', 'SLEEVES', 'Nos', 5, 2.75, '490110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126003', 'ZERO MAIDA WHOLE WHEAT BREAD SLEEVE', 'SLEEVES', 'Nos', 5, 2.75, '490110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126004', 'BREAD SLEEVES (HIGH PROTIN MILK)', 'SLEEVES', 'Nos', 5, 2.75, '490110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126005', 'ZERO MAIDA HIGH PROTEIN WHOLE WHEAT', 'SLEEVES', 'Nos', 5, 2.75, '490110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126006', 'ZERO MAIDA HIGH PROTEIN MULTIGRAIN', 'SLEEVES', 'Nos', 5, 2.75, '490110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD112010', 'PLAIN BREAD POUCH (200GM)', 'BREADPOLY', 'Kgs', 18, 125, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125001', 'IDLI DOSA BATTER 450GM (PLAIN STANDY)', 'POUCHES', 'Nos', 18, 3, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125002', 'IDLI DOSA BATTER 900GM POUCH', 'POUCHES', 'Nos', 18, 5.6, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125003', 'LD COUNTRY DELIGNT PLAIN POUCH', 'POUCHES', 'Nos', 18, 195, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125004', 'RAVA IDLI BATTAR POUCH 450', 'POUCHES', 'Nos', 18, 2.5, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125005', 'Chocolate Inner Pouch(75x100)', 'POUCHES', 'Nos', 18, 270, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125006', 'Chocolate Inner Pouch(45x125)', 'POUCHES', 'Nos', 18, 270, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125007', 'POUCH PREMIUM TRAIL', 'POUCHES', 'Nos', 18, 270, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125008', 'POUCH 7-IN-1 SUPER MIX', 'POUCHES', 'Kgs', 18, 270, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125009', 'MAKHANA 20GM (4 LAYER)', 'POUCHES', 'Kgs', 18, 250, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125010', 'ALMOND-PINK SALT (4 LAYER)', 'POUCHES', 'Kgs', 18, 250, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125011', 'ALMOND-TANDOORI FLAVOUR (4 LAYER)', 'POUCHES', 'Kgs', 18, 250, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125012', 'CASHEW-PINK SALT (4 LAYER)', 'POUCHES', 'Kgs', 18, 250, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125013', 'CASHEW- SALT & PEPPER (4 LAYER)', 'POUCHES', 'Kgs', 18, 250, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125014', 'CASHEW-TANDOORI FLAVOUR (4 LAYER)', 'POUCHES', 'Kgs', 18, 250, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125015', 'PEANUT-PINK & BLACK SALT (4 LAYER)', 'POUCHES', 'Kgs', 18, 250, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125016', 'PEANUT-SOMKY BARBECUE(4LAYER)', 'POUCHES', 'Nos', 18, 250, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125017', 'PEANUT-TANDOORI PEANUTS(4LAYER)', 'POUCHES', 'Kgs', 18, 250, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125018', 'YUMMY CHOCOLATE MAKHANA (4 LAYER)', 'POUCHES', 'Kgs', 18, 250, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125019', 'CHOCOLATE POUCH (GOLDEN COLOUR)', 'POUCHES', 'Kgs', 18, 280, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125020', 'GENERIC SNACKS POUCH 150GM (4 LAYER', 'POUCHES', 'Kgs', 18, 240, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125021', 'GENERIC SNACKS POUCH 100GM (4 LAYER', 'POUCHES', 'Kgs', 18, 240, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125022', 'IDLI DOSA BATTER 450GM QR CODE', 'POUCHES', 'Nos', 18, 2.5, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125023', 'MALABAR PAROTA POUCH (240GM)', 'POUCHES', 'Kgs', 18, 255, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125024', 'HOME STYLE INDIAN SNACKS', 'POUCHES', 'Kgs', 18, 225, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125025', 'WHEAT LACHHA PARATHA 240GM', 'POUCHES', 'Kgs', 18, 240, '39232990');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111031', 'ROASTED MAKHANA CHATPATA PUDINA 25G', 'POLYFILM', 'Kgs', 18, 350, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111032', 'ROASTED MAKHANA SALT&PEPPER 25GM', 'POLYFILM', 'Kgs', 18, 250, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111033', 'ROASTED MAKHANA CHEESE 25GM', 'POLYFILM', 'Kgs', 18, 350, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111034', 'ROASTED MAKHANA GHEE TURMERIC 25GM', 'POLYFILM', 'Kgs', 18, 370, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111035', 'ROASTED MAKHANA HIMALAYAN PINK SALT', 'POLYFILM', 'Kgs', 18, 370, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111036', 'DISNEY ROASTED MAKHANA CHATPATI IML', 'POLYFILM', 'Kgs', 18, 250, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111037', 'DISNEY ROASTED MAKHANA GHEE TURMERI', 'POLYFILM', 'Kgs', 18, 370, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111038', 'DISNEY ROASTED MAKHANA CHEESE 25GM', 'POLYFILM', 'Kgs', 18, 370, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111039', 'Allu Bhujia', 'POLYFILM', 'Kgs', 18, 220, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111040', 'DRY BHEL', 'POLYFILM', 'Kgs', 18, 220, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111041', 'MIX FARSEN', 'POLYFILM', 'Kgs', 18, 220, '39206919');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125026', 'VEG NOODLES POUCH (150GM)', 'POUCHES', 'Nos', 18, 1.05, '39239090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119002', 'DEEP STABILIZER BLEND', 'INGREDNT', 'Kgs', 18, 3000, '13019045');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119003', 'DEEP ENZYMES BLEND', 'INGREDNT', 'Kgs', 18, 3500, '35079069');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119004', 'PROVA VANILLA FLAVOR', 'INGREDNT', 'Kgs', 18, 1600, '33021090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125027', 'IDLI DOSA BATTER 450GM (HP)', 'POUCHES', 'Nos', 18, 3.9, '39219099');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD118001', 'Cow Ghee Jar 1000ML', 'GLASSJAR', 'Nos', 18, 17.55, '70109000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD120001', 'Cow Ghee Lid(Without printed cap)', 'JARLID', 'Nos', 18, 3, '83099090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD120002', 'Cow Ghee Lid(printed cap)', 'JARLID', 'Nos', 18, 3.55, '83099090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126007', 'Cow Ghee Shrink Sleeves(900ml)', 'SLEEVES', 'Nos', 18, 1.52, '3920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126008', 'Cow Ghee Shrink Sleeves Neck', 'SLEEVES', 'Nos', 18, 0.26, '3920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD114001', 'Corrugated box (4pcs)', 'CORRBOX', 'Nos', 5, 17.5, '49191010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126009', 'Cow Ghee Shrink Sleeves(880ml)', 'SLEEVES', 'Nos', 18, 1.52, '3920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD114002', 'Corrugated box (1pcs)', 'CORRBOX', 'Nos', 5, 8, '49191010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD118002', 'GLOBAL JAR 525ML', 'GLASSJAR', 'Nos', 18, 13.55, '70109000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126010', 'COW GHEE SHRINK SLEEVES(400ML)', 'SLEEVES', 'Nos', 18, 0.85, '3920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126011', 'Cow Ghee Shrink Sleeves(850ml)', 'SLEEVES', 'Nos', 18, 1.52, '3920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD118003', 'GHEE JAR (810ML)', 'GLASSJAR', 'Nos', 18, 18.25, '70109000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126012', 'Buffalow GHEE SHRINK SLEEVES(850ML)', 'SLEEVES', 'Nos', 18, 1.52, '3920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126013', 'Buffalow GHEE SHRINK SLEEVES(900ML)', 'SLEEVES', 'Nos', 18, 1.52, '3920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126014', 'Buffalo GHEE SHRINK SLEEVES(400ML)', 'SLEEVES', 'Nos', 18, 0.78, '3920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD126015', 'COW GHEE SHRINK SLEEVES(700ML)', 'SLEEVES', 'Nos', 18, 1.23, '3920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD127001', 'GHEE SECURE SEAL STICKER CM', 'STICKER', 'Nos', 18, 0.59, '482110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD127002', 'GHEE SECURE SEAL STICKER BM', 'STICKER', 'Nos', 18, 0.59, '482110');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD114003', 'Corrugated box (8pcs) 400ML BM', 'CORRBOX', 'Nos', 5, 18.25, '49191010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD114004', 'Corrugated box (8pcs) 400ML CM', 'CORRBOX', 'Nos', 5, 18.25, '49191010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119005', 'ATTA RAJDHANI', 'INGREDNT', 'Kgs', 0, 32.6, '11010000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119006', 'MAIDA RAJDANI', 'INGREDNT', 'Kgs', 0, 33.6, '11010000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119007', 'ALSI SABUT', 'INGREDNT', 'Kgs', 5, 133.33, '9101130');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119008', 'PUMKIN SEEDS', 'INGREDNT', 'Kgs', 5, 425, '20081920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119009', 'WHITE TIL (SESAME SEEDS)', 'INGREDNT', 'Kgs', 5, 188, '12071010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119010', 'SUNFLOWER SEEDS', 'INGREDNT', 'Kgs', 5, 170, '12060010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119011', 'TARBOOJ SEEDS', 'INGREDNT', 'Kgs', 5, 675, '12077090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119012', 'ATTA JO (BARLEY)', 'INGREDNT', 'Kgs', 5, 62.86, '11010000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119013', 'BAJRA ATTA', 'INGREDNT', 'Kgs', 5, 57.14, '11010000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119014', 'RAGI ATTA', 'INGREDNT', 'Kgs', 5, 75.24, '11010000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119015', 'ATTA RICE', 'INGREDNT', 'Kgs', 5, 59.05, '11010000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119016', 'GLUTEN', 'INGREDNT', 'Kgs', 5, 160, '11090000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119017', 'ICING SUGER', 'INGREDNT', 'Kgs', 5, 55, '17011490');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119018', 'BESAN RAJDHANI', 'INGREDNT', 'Kgs', 5, 109.52, '11062010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119019', 'HONEY', 'INGREDNT', 'Kgs', 5, 284.76, '4090000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119020', 'MILK MAID (CONDENSED MILK)', 'INGREDNT', 'Nos', 5, 137.14, '4029920');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119021', 'SOOJI (500GM)', 'INGREDNT', 'Nos', 5, 30.95, '11010000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119022', 'SUNFLOWER OIL', 'INGREDNT', 'Ltr', 5, 161.9, '12071010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119023', 'MILK POWDER (WHITENER)', 'INGREDNT', 'Kgs', 5, 570.48, '4022910');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119024', 'CORNFLOUR STARCH(WEIKFIELD)', 'INGREDNT', 'Kgs', 5, 98.215, '24011090');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119025', 'TATA SALT', 'INGREDNT', 'Kgs', 0, 28, '25010020');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119026', 'SUGER', 'INGREDNT', 'Kgs', 5, 46.67, '17011490');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119027', 'IDLY RICE BOILED', 'INGREDNT', 'Kgs', 0, 50.5, '10063010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119028', 'SOYA ATTA', 'INGREDNT', 'Kgs', 5, 94.29, '12081000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119029', 'URAD DAL SABUT', 'INGREDNT', 'Kgs', 0, 141, '7139010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119030', 'ELAICHI GREEN (CORDAMOM)', 'INGREDNT', 'Kgs', 5, 3619.05, '9101130');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119031', 'FERMANTED WHEAT FLOUR', 'INGREDNT', 'Kgs', 5, 520, '1101');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119032', 'COLD PRESSED SUNFLOWER OIL', 'INGREDNT', 'Ltr', 5, 210, '15121910');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119033', 'GREEN ELAICHI POWDER', 'INGREDNT', 'Ltr', 5, 4285.71, '9101130');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119034', 'HULLED OATS SEEDS', 'INGREDNT', 'Ltr', 5, 70, '11042200');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119035', 'SUNFLOUR OIL TIN (NPD) 13LTR', 'INGREDNT', 'Nos', 5, 2228.57, '15121910');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119036', 'MOONG DAL (SABUT)', 'INGREDNT', 'Kgs', 0, 135, '7133100');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119037', 'ROLLER MILL ATTA', 'INGREDNT', 'Kgs', 0, 31.8, '11010000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119038', 'Oxyguard 20CC', 'INGREDNT', 'Nos', 18, 1.35, '38249900');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD111042', 'FOOD WRAP', 'POLYFILM', 'Kgs', 18, 299.15, '48062000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD128001', 'Tissue Tape Double Side', 'TAPES', 'Nos', 18, 81.53, '39199010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119039', 'Roasted Barley Malt Flour', 'INGREDNT', 'Kgs', 5, 165, '11072000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119040', 'CASHEW NUT', 'INGREDNT', 'Kgs', 5, 850, '8013210');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119041', 'FERMENTED GLUCOSE NATURAL  ASCORBIC', 'INGREDNT', 'Kgs', 5, 780, '110290');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119042', 'SKIMMED MILK POWDER', 'INGREDNT', 'Kgs', 5, 285, '4021010');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119043', 'Pistachio', 'INGREDNT', 'Kgs', 5, 3300, '8025100');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119044', 'Isolated Soya Protein', 'INGREDNT', 'Kgs', 18, 300, '35040091');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119045', 'Pea Protein Consentrate', 'INGREDNT', 'Kgs', 5, 365, '21061000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD125028', 'Moong Chilla Batter Standup Pouch', 'POUCHES', 'Nos', 18, 4.75, '101828');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119046', 'METHI DANA', 'INGREDNT', 'Kgs', 5, 123.81, '9101130');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119047', 'POHA', 'INGREDNT', 'Kgs', 5, 72, '11010000');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119048', 'PLANT ISOLATE PROTEIN', 'INGREDNT', 'Kgs', 18, 335, '35040099');
INSERT INTO item_master (item_id, item_desc, group_id, unit_id, gst_perc, cost_price, hsn_code) VALUES ('BD119049', 'FORTIUM R40 LIQUID', 'INGREDNT', 'Kgs', 18, 3500, '33021090');


-- ====================================================================
-- 5. SUPPLIER MASTER (33 suppliers)
-- ====================================================================

INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('RNGTHERMO', 'R.N. THERMO PACK PVT. LTD.', 'NO. 52-B, BOMMASANDRA INDUSTRIAL AREA', 'ANEKAL TALUK', 'Bengaluru', '8105114575');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('VANDHANA', 'VANDHANA POLY-PLAST PVT LTD', 'Plot No. 113, Sector - 6A, I.I.E.,', 'SIDCUL, Haridwar - 249402', 'UTTARAKHAND', '9560095727');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('SHRIRAM', 'SHRI RAM INDUSTRIES', 'F-64 SECTOR-5 BAWANA', 'INDL AREA DELHI-110039', 'DELHI', '9250904161');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('LALIT', 'LALIT PRINTERS', 'shop no- 18 new colony', 'Gurugram haryana -122006', 'GURGAON', '9810526099');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('SHRIANAND', 'SHRIANAND POLY', 'Begumpur Khatola', 'Near Saraswati International School,', 'GURGAON', '9910020886');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('SGV', 'SGV PACKK LLP', 'Plot No.37, Sikri Harphala Road', 'Ballabgarh, Faridabad-121004', 'FARIDABAD', '9643318323');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('MAGNUS', 'MAGNUS PACKAGING', 'No.192, Yarandahalli Village', 'Jigani Hobli, Anekal Taluk', 'BENGALURU', '9845794570');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('SRIRAM', 'SRI RAM FLEXIBLES PVT LTD', 'KHASRA NO. 127, RAIPUR INDUSTRIAL AREA', 'BHAGWANPUR, HARIDWAR, UTTARAKHAND, 247661', 'UTTARAKHAND', '7417489025');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('MEHTA', 'MEHTA FLEX LLP', '65/3 D 5, SURVEY NO. 65/3 D', 'RINGANWADA, RINGANWADA', 'DAMAN DIU', '9004077089');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('GARIMA', 'GARIMA POLYMERS', 'e 200 shastri nagar near indra park', 'opp metro piller 180 new delhi 110052', 'NEW DELHI', '9899333503');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('REEDHAM', 'REEDHAM ENTERPRISES', 'C4 1st Floor Dayal Bagh', 'Police Chowki Sec.39 Surajkund', 'DELHI', '9718646857');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('MAYAAG', 'MAYA AGROVET', 'Reg. add.:Laxminarayanpura, Khasra No. 658', 'Akhepura, Harmada, Jaipur, Rajasthan, 302028', 'JAIPUR', NULL);
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('OSHOIND', 'OSHO INDUSTRIES', 'KHASRA NO. 100 RAIPUR INDUSTRIAL AREA', 'TEH ROORKEE BHAGWANPUR HARIDWAR UTTRAKHAND -247661', 'UTTARAKHAND', '9711616031');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('AERO', 'AERO PLAST', '2420, Industrial Area', 'MIE Part-B, Bahadurgarh, Haryana 124507', 'HARYANA', '7419310028');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('DE3PBIO', 'DE3PBIO TECHNOLOGIES', 'PH 1, A 506 BLDG No 3, RUTU RIVERSIDE ESTATE', 'NR K M AGRAWAL COLLEGE, KALYAN W, KALYAN', 'MAHARASHTRA', '9108557072');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('SREEVEN', 'SREEVEN THE OFFSET PRINTER', 'Plot No C1, Block III, IDA Uppal', 'Hyderabad, Telangana - 500 039', 'TELANGANA', '9866423015');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('AJANTA', 'AJANTA BOTTLES', 'Flat No.: B-226, NARAINA INDUSTRIAL AREA', 'PHASE-1, New Delhi, PIN Code: 110028', 'NEW DELHI', '9811577815');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('PROGRESSV', 'PROGRESSIVE PACKAGING', 'N-25, SECTOR 2', 'DSIIDC BAWANA INDUSTRIAL AREA,', 'DELHI', '9654949988');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('RRIND', 'RR INDUSTRIES', 'VILLAGE BEGUMPUR KHATOLA, SECTOR - 74', 'NEAR GRAMIN BANK GURUGRAM, HARYANA', 'GURGAON', '9810024696');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('GANGA', 'GANGA ROLLER FLOUR', 'B-37 LAWRENCE ROAD', 'INDUSTRIAL AREA', 'DELHI', '9643420426');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('SSM', 'SODHI SUPER MARKET', 'SCO No. 141-142,', 'Sector -46 Gurgaon-Haryana', 'GURGAON', '9266537776');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('RTC', 'RTC FOODS', '163, PHASE 5, KUNDLI INDUSTRAIL AREA', 'HSIIDC, SONIPAT HARYANA 131028', 'HARYANA', '9711939721');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('ASHA', 'ASHA RAM AND SONS', '3496, Desh Bandhu Gupta, Dariba Pan', 'Paharganj, Central Delhi, Delhi, 110055', 'DELHI', '9718899994');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('NIKHILAND', 'MS NIKHILANAND TRADING', 'D.no. 2-1251/2 B2 Nikhilananda apartment', 'Nalanga nagar, B.v.Reddy colony', 'TAMILNADU', '7013609359');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('KCENTR', 'KC ENTERPRISES', 'B-119/3 MUNIRKA VILLAGE', 'NEW DELHI 110067', 'NEW DELHI', '9037638309');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('PARAMOUNT', 'PARAMOUNT FOODS', 'Plot 301, Barhi Industrial Area', 'Gannaur, Sonipat-131101', 'HARYANA', '9958780936');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('VSNATUR', 'VS NATURAL AGRO FOODS', 'Vs Natural Agro Foods 383A, Bhandure Mala', 'Dadoba Road, Bajarang Nagar, Satpur Nashik', 'MAHARASHTRA', '9049008874');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('ANKITA', 'ANKITA AGRO & FOOD PROCESSING', 'PLOT NO. F-40-41, EPIP RIICO INDUSTRIAL AREA', 'NEEMRANA, ALWAR, RAJASTHAN-301705', 'RAJASTHAN', '9845756118');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('PURENFR', 'PURE N FRESH', 'New Anaj Mandi', 'Khandsa Road', 'GURGAON', '9312629486');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('VIJAYALA', 'VIJAYALAKSHMI DALL MILL', 'Burripalem Road, 497/4 and II Unit, Nelapadu', 'Tenali, Guntur, Andhra Pradesh, 522201', 'Andhra Pradesh', '8074306515');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('KEMIN', 'KEMIN INDUSTRIES', 'Plot No K-3, 11th Cross Street SIPCOT Industrial', 'Complex Gummidipundi-601201, Tamil Nadu, India', 'Tamil Nadu', '8209959759');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('HIMALAYA', 'Himalaya Pack & Stationery', 'Shop No-5, Sony Complex,', 'Prashanti Nagar, Kukatpally, I.E Hyderabad-500072', 'Hyderabad', '9989332929');
INSERT INTO supplier_master (supp_id, supp_name, address_1, address_2, address_3, mob_no) VALUES ('AGIGRN', 'AGI GREENPACK LTD', 'Box 1930, Glass Factory road, Off Moti Nagar', 'Sanatnagar, HYDERABAD-500 018', 'Hyderabad', NULL);


-- ====================================================================
-- VERIFICATION QUERIES (Run after insert)
-- ====================================================================

-- SELECT COUNT(*) as branch_count FROM branch_master;
-- SELECT COUNT(*) as group_count FROM group_master;
-- SELECT COUNT(*) as unit_count FROM unit_master;
-- SELECT COUNT(*) as item_count FROM item_master;
-- SELECT branch_id, branch_name FROM branch_master;
-- SELECT group_id, group_desc FROM group_master;
-- SELECT item_id, item_desc, group_id, cost_price FROM item_master LIMIT 10;
