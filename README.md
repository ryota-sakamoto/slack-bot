# Slack Bot

## Scala Online Compile
### Message
```
scala:object Main {  
    def main(args: Array[String]): Unit = {  
        f((2 to 100).toList, 2).foreach(printf("%d ", _))  
    }  
    def f(l: List[Int], c: Int): List[Int] = {  
        val n_l = l.filter(x => x == c || x % c != 0)  
        if (l.length == n_l.length) {  
            n_l  
        } else {  
            f(n_l, n_l.filter(_ > c).head)  
        }  
    }  
}
```

### Result
```
{
    "program_message" : "2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71 73 79 83 89 97 ",
    "program_output" : "2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71 73 79 83 89 97 ",
    "status" : "0"
}
```

## Search Train
### Message
```
train:吉祥寺-横浜
```
### Result
```
720円 1時間13分 1回
04:34発 吉祥寺
05:12着05:20発 東京
05:47着 横浜
```

## Set Timer
### Message
```
set:2355 message
```
### Result (23:55:00)
```
message
```