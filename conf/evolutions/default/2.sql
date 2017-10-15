# --- Sample dataset

# --- !Ups

insert into bookmark (id,name,url,slug,userId) values (1,'Google', 'http://www.google.com', 'rXEonA', 0);
insert into bookmark (id,name,url,slug,userId) values (2,'Yahoo', 'http://www.yahoo.com','pMVlCT', 0);
insert into bookmark (id,name,url,slug,userId) values (3,'CNN', 'http://www.cnn.com','seEPvG', 0);
insert into bookmark (id,name,url,slug,userId) values (4,'Facebook', 'http://www.facebook.com','fTuDE7', 0);

insert into bookmark (id,name,url,slug,userId) values (5,'YNET', 'http://www.ynet.co.il', 'DE7fTu', 1);
insert into bookmark (id,name,url,slug,userId) values (6,'NRG', 'http://www.nrg.co.il','PvGseE', 1);


insert into user (userId,email) values (0,'shakhalevinson@gmail.com');
insert into user (userId,email) values (1,'ohad@appthis.com');

insert into token (userId,token,createdAt) values (0,'koko', NOW());
insert into token (userId,token,createdAt) values (1,'momo', NOW());


# --- !Downs

delete from bookmark;
delete from user;
