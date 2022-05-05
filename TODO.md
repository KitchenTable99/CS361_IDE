# TODO

## General Project 10 Shit

* Update keyword highlighting in the IDE
* ~~Replace "Compile" and "Compile & Run" with "Check"~~
    * ~~**NB:** This will require the Project 6 refactor to have already happened~~

## Increase Bantam Java Syntax

* ~~Variable type declaration~~
    * ~~Scanner~~
    * ~~Parser~~
    * ~~Semantic Analyzer~~
    * Create example for Dale
    * Catalog running instructions for Dale
* Cast Syntax
    * Scanner
    * Parser
    * Semantic Analyzer
    * Create example for Dale
    * Catalog running instructions for Dale

## Finalize New Scanner (for Caleb)

* ~~Move getNextNonWhitespaceChar to SourceFile~~
* ~~Pass SourceFile to TokenBuilderFactory~~
* ~~Bypass Scanner altogether~~

## Apply Dale's Feedback From Project9

* If there is a local var named “boo” but no field named “boo”, then “this.boo” should be illegal. Your code doesn’t
  catch this.
* Your checker doesn’t check the number and types of actual parameters to see if they match in number and are subtypes
  of the formal parameters when visiting a DispatchExpr.
* Your checker allowed DeclStmts of the form “var x = null;” and “var x = foo();” where foo is a void method.
* Your checker allows reserved words like “boolean” as variable names.
* Your checker allows declarations such as “var x = varr;” where varr is an undeclared variable.
* Your checker doesn’t allow a for stmt of the form “for(;;){}”, even though it is legal.
* Your checker allows 2 local vars of the same name with overlapping scope if one is declared at a more nested level
  than the other.
* Your checker ignores the refName field of AssignExpr nodes and so checks the wrong variable sometimes.
* Your visit(DispatchExpr) searches for the method name in the class’s varSymbolTable instead of its methodSymbolTable.
  As a result, it reports that the String class doesn’t have a toString() method.

## Apply Dale's Feedback From Project6

* ~~Switch the controller pieces~~
* ~~You created new classes ProcessBuilderShuttle and SaveInformationShuttle, which are good ideas to minimize functions
  with side effects. But the use of the ProcessBuilderShuttle is awkward. A simpler approach is to have the
  handleCompile method call tabController’s readyForCompile to see if it should proceed. Then in handleCompile, have the
  TabController return the file path of the selected tab, and then have handleCompile create and initialize the
  ProcessBuilder and then call doCompiling. This approach eliminates the need for the ProcessBuilderShuttle class.~~
~~* JavaCodeArea is still not a good name for your class because it implies that it extends CodeArea, which it doesn’t.
  You can’t add it to a tab, for example.~~
~~* SaveFailureException is a perfectly acceptable subclass of Exception, but there should be documentation (internal or
  external or in the report above) saying what it is used for. The Javadoc header doesn’t give any helpful information
  in that regard.~~
~~* Minimize negatives like “not” in method names to prevent the need for double negatives. For example, the method
  notReadyForP should be changed to isReadyForP and then you can use instructions like “if (! isReadyForP())...” like
  you do with the other “Ready” methods.~~

# What we did

* Variable Type Declaration
* Cast Syntax
* Elegantly fixed project 6
* Elegantly fixed project 9
* Made the given scanner class so elegant we deleted it