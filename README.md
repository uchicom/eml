# eml
Mailer

## mvn
### 起動
```
mvn exec:java "-Dexec.mainClass=com.uchicom.eml.Main"
mvn exec:java "-Dexec.mainClass=com.uchicom.eml.Main" "-Dexec.args='Email File Path'"
```

## jar実行
```
java -jar ./target/eml-0.0.1-SNAPSHOT-jar-with-dependencies.jar
java -jar ./target/eml-0.0.1-SNAPSHOT-jar-with-dependencies.jar "Email File Path"
```
