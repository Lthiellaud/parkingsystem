/* Column DISCOUNT added in table ticket to store discount indicator calculated during the incoming process*/
alter table test.ticket
Add column DISCOUNT boolean after OUT_TIME;

alter table prod.ticket
Add column DISCOUNT boolean after OUT_TIME;