create table nextEvents (
	_id integer primary key autoincrement,
	contactId integer not null,
  	contactName text not null,
  	nameDay text not null,
  	birthdayYear text null,
  	eventWhen text not null,
  	checkedDate text not null);
