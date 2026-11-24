entityManager
.createQuery("from Client", Client.class)
.getResultList();

56600 ns acquiring 1 JDBC connections
47300 ns releasing 1 JDBC connections
1152600 ns preparing 22 JDBC statements
38251700 ns executing 22 JDBC statements

---

entityManager
.createQuery("SELECT c FROM Client c JOIN FETCH c.accounts", Client.class)
.getResultList();

47400 ns acquiring 1 JDBC connections
34500 ns releasing 1 JDBC connections
141600 ns preparing 1 JDBC statements
3368900 ns executing 1 JDBC statements