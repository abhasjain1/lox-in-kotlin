Kotlin implementation of Lox programming language from [Crafting Interpreters](https://craftinginterpreters.com/). 

The language grammar is taken from the book. 

1.) The `Scanner.kt` converts the source code to Tokens `Token.kt`.

2.) `Parser.kt` converts raw tokens to an AST (Abstract Syntax Tree). This outputs a List of Statements `Stmt.kt`. 

3.) `Interpreter.kt` runs the program by moving down the AST via tree-walk.

## Current Support

* Expressions
* Variables 
* Blocks
* Conditions (Flow Control)
* Blocks / Scope
* Print Statements
* Comments

## Example Program 

```
var a = 3;
var b = 5 + a;
a = 9;

print a;

if (3 + 8 == 11) {
   print "yes";
}

if (6 * 7 == 40) {
  print "This should not be printed!";
} else {
  print "Print this!!";
}

{
   var b = 77;
   print b;
}

print b;
// print a + 3;
```

### Output:

```
9
yes
Print this!!
77
8
```

## Working on

* Functions
* Loops
* Classes
* Data Structures (Array, Map for now)
* Error handling from the compiler - I should have looked into this from the start :(
