keywords: prefixed by @
@def: function definition
@typealias: typealias definition

## Datatypes

* any:
    * type-symbol: `^`
    * ex: represents all types

* nothing:
    * type-symbol: `.`
    * ex: placeholder to signify that there is no types (remove ambiguity in function signature)

### Primitives

* Boolean:
    * type-symbol: `!`
    * ex: @true, @false

* Number:
    * type-symbol: `#`
    * ex: 0, 2, -4, 192, 2.1, 29.3, -12.1

* String:
    * type-symbol: `"`
    * ex: "ABC def", "Pourquoi?", "Hello, World!"

* Char:
    * type-symbol: `'`
    * ex: 'a', '0', '\n', '\u00E9', 'Z'

### Complex

`(T, U and V are type-symbols)`

* Array:
    * Syntax:
        * Array of primitive values are constructed by putting a space between each value:   
          ex: `1 2 3`, `"a" 12.2 33`, `0 'q' 2`
    * type-symbol syntax: `[T` OR `[`
    * Note: if T is missing, that's the same as writing `[^`

    * Type examples:
        * generic array: `[`
        * array of type T: `[T`

* Function:
    * type-symbol syntax: `(T,U>V`
        * Notes:
            1) If T or U or V is missing, that's the same as replacing the letter with `^` (the "any" type-symbol)

            2) If the `,` is missing (and, obviously, the `T`), it is assumed that the function is a prefix function,
               i.e. `T` is replaced with `.` (the "nothing" 1type-symbol), i.e.
                - `(#>[` == `(.,#>[`
                - `(>#` == `(.,^>#`
                - `(>` == `(.,^>^`

            3) The `>` is **required** to prevent ambiguous situations

    * Type examples:
        * a prefix function that takes anything and returns nothing: `(>.` == `(.,^>.`
        * an infix function that takes anything and returns anything: `(,>` == `(^,^>^`
        * function that takes two numbers and return a number: `(#,#>#`
        * a mapping function: `((>,[>[` == `((.,^>^,[^>[^`, which means:
            * An infix function with:
                * Left arg is a prefix function that takes anything and returns anything
                * Right arg is an array of any values
                * Return type is an array of any values

## Type-symbols

- They are prefix to variable or parameters to mark them as a certain type

    - In function definition, they allow function overloading because the type-symbols are included in the function's
      signature

### Function's signature:

1) map: `@def (#>"fonc . [#arr > ["`
    - Signature: `.@((.,#>",[#>["`

2) map: `@def (>.fonc . ["arr > [`
    - Signature: `.@((.,^>.,[">[^`

3) map: `@def . [arr`
    - Signature: `.@(.,[^>^`

4) map: `@def fonc . arr`
    - Signature: `.@(^,^>^`

5) filter: `@def (>!fonc ↘ [arr > [`
    - Signature: `↘@((.,^>!,[^>[^`

6) foreach: `@def (^>.fonc ↺ [arr > .`
    - Signature: `↺@(.,^>.,[^>.`

7) increment: `@def ↑ [#arr > [# = @_ + 1 . arr`
    - Signature: `↑@(.,[#>[#`

### Functions

- A function can have any name except:
    - `@` (reserved symbol for keywords)
    - `=` (reserved symbol for assignment)
    - a number at the start of the function name
    - `'` or `"` at the start or the end of the function name
    - `(`, `)`, `[`, `]`, `{`, `}` because they are used to structure the language

#### Function overloads

- A function may have overload, with the following restriction:

    1. The overloads may not only differ in return type; they need to differ in argument types and/or in function type
       (infix, postfix or prefix)

### Function call

#### Normal call

- A normal function call occurs when a function call's signature matches a function signature. If the function call's
  signature matches more than one function signature, the one with the least number of `^` (any) is chosen.

- If there are more than one function signature that match the given function call's signature with the same number
  of `^`, an error is thrown and the program ends execution because of overload ambiguity.

#### Partial call

- A partial call occurs when an infix function is called with only one of its arguments and the result is a partial
  function.

- A partial call can be forced (in case of ambiguity) with the use of the `@_` keyword at the unknown part of the call
- Ex:
    - Say the `↑` is a function with a postfix and an infix overload. If we want a partial call to the _infix overload_,
      we can't do `1↑` because that would call the _postfix overload_. In this case, we need to do `1 ↑ @_` to
      force the function to be a partial infix call.

### Partial Functions:

- A partial function has a [partial signature](#signature-of-a-partial-fonction-).

- When called with the complementary argument, the partial function resolves the function signature, finds the
  corresponding function to call, and calls it.

#### Signature of a Partial Fonction:

- A function's partial signature is generated on a partial function call.
- It starts with `~@` and contains the symbol `_` on the unresolved parts of the signature.
- Ex:
    - Say we have the following expression: `add1 = 1 +`. The variable `add1` will contain a partial function with:
        - left arg: `1`
        - partial signature: `~@+@(#,_>_`
    - Then, if we write something like `add1 3`, we resolve the function's signature and get a match with the `+`
      function with the signature `+@(#,#>#`. We can call this function to get our result: `4`!
    - If we do the same exercice with the code `add1 " friend"` we resolve to get `+@(#,">")` and we get the
      result: `"1 friend"`!

### Type compatibility:

Let `T` be the matcher type and `U`, the matched type. `U` is compatible with `T` when:

- `T` is `^` (any) and `U` is not `.` (nothing)
- `T` and `U` are both primitive types and:
    1. `T` == `U`
- `T` and `U` are both complex types and:
    1. `T` is the same _surface type_ as `U` (ex: both are arrays or functions);
    2. If `T` and `U` are functions, their names are the same;
    3. Each type argument in the complex type of `T` is compatible with the corresponding type argument in the complex
       type of `U` .

