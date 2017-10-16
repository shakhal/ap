# --- Sample dataset

# --- !Ups

insert into bookmark (id,name,url,slug,userId) values (1,'Google', 'http://www.google.com', 'rXEonA', 0);
insert into bookmark (id,name,url,slug,userId) values (2,'Yahoo', 'http://www.yahoo.com','pMVlCT', 0);
insert into bookmark (id,name,url,slug,userId) values (3,'CNN', 'http://www.cnn.com','seEPvG', 0);
insert into bookmark (id,name,url,slug,userId) values (4,'Facebook', 'http://www.facebook.com','fTuDE7', 0);

insert into bookmark (id,name,url,slug,userId) values (5,'YNET', 'http://www.ynet.co.il', 'DE7fTu', 1);
insert into bookmark (id,name,url,slug,userId) values (6,'NRG', 'http://www.nrg.co.il','PvGseE', 1);


insert into user (userId,email) values (0,'test1@appthis.com');
insert into user (userId,email) values (1,'test2@appthis.com');
insert into user (userId,email) values (2,'test3@appthis.com');

insert into token (userId,token,createdAt) values (0,'KvDEqBDZ4faGJiasN4Bpz6HtD0B1Za6pkcJ5BwJFNjLFNRpAtY', NOW());
insert into token (userId,token,createdAt) values (1,'T1Gm2pd8dO0H3zqj96ipjt3R88OU0JjF3evMxWIJZGLzaAh0Zv', NOW());
insert into token (userId,token,createdAt) values (2,'WVEq0gZtZsye8wtCGKWCPR2AnDyqWD6KiuooBpXQdMPg1aehvD', NOW());


# --- !Downs

delete from bookmark;
delete from user;
delete from token;