/* Protocol definition file for an RPC based user lookup application */

/* There are 2 remote procedures -
	byuid - returns a user name given the uid number
	byname - returns a uid given a user name
*/

/* Need to define a string type here with max length
	(remember about needing a wrapper for the
	xdr string filter?)
*/

typedef string username<10>;



program ULKUP_PROG {
   version ULKUP_VERSION {
     int byname(username) = 1;
     username bynum(int) = 2;
   } = 1;
} = 555555556;

