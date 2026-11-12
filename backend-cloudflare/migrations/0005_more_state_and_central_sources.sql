-- Migration 0005: additional central and state recruitment sources.
--
-- Already present in 0001 and therefore skipped: SSC (ssc.nic.in now redirects to ssc.gov.in),
-- Indian Railways, UPSC, DRDO, Join Indian Army, Join Indian Navy, ISRO, and IAF careers.
--
-- NCS and MPOnline are seeded disabled: their landing pages are portals rendered with
-- JavaScript and the generic link scraper finds no individual notices on them. They need
-- a dedicated parser (or a more specific listing_url) before being enabled.

INSERT OR IGNORE INTO job_sources (id, name, official_domain, listing_url, parser_type, state, enabled, polling_interval_hours) VALUES
-- ALL INDIA
('src-ncs', 'National Career Service (NCS)', 'ncs.gov.in', 'https://www.ncs.gov.in/', 'API', 'All India', 0, 12),
('src-bsf', 'Border Security Force Recruitment (BSF)', 'rectt.bsf.gov.in', 'https://rectt.bsf.gov.in/', 'HTML_TABLE', 'All India', 1, 12),
('src-coastguard', 'Indian Coast Guard Recruitment', 'joinindiancoastguard.gov.in', 'https://joinindiancoastguard.gov.in/', 'HTML_TABLE', 'All India', 1, 12),

-- STATE AUTHORITIES
('src-assampolice', 'Assam Police Recruitment', 'police.assam.gov.in', 'https://police.assam.gov.in/', 'HTML_TABLE', 'Assam', 1, 12),
('src-aphc', 'High Court of Andhra Pradesh', 'aphc.gov.in', 'https://aphc.gov.in/', 'HTML_TABLE', 'Andhra Pradesh', 1, 12),
('src-ojas', 'OJAS Gujarat (Online Job Application System)', 'ojas.gujarat.gov.in', 'https://ojas.gujarat.gov.in/', 'HTML_TABLE', 'Gujarat', 1, 6),
('src-gpsc', 'Gujarat Public Service Commission (GPSC)', 'gpsc-ojas.gujarat.gov.in', 'https://gpsc-ojas.gujarat.gov.in/', 'HTML_TABLE', 'Gujarat', 1, 6),
('src-rpsc', 'Rajasthan Public Service Commission (RPSC)', 'rpsc.rajasthan.gov.in', 'https://rpsc.rajasthan.gov.in/', 'HTML_TABLE', 'Rajasthan', 1, 6),
('src-mponline', 'MPOnline Recruitment Portal', 'mponline.gov.in', 'https://mponline.gov.in/portal/', 'HTML_TABLE', 'Madhya Pradesh', 0, 12),
('src-uppsc', 'Uttar Pradesh Public Service Commission (UPPSC)', 'uppsc.up.nic.in', 'https://uppsc.up.nic.in/', 'HTML_TABLE', 'Uttar Pradesh', 1, 6);
