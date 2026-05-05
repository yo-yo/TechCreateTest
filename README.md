# TechCreateTest
Test for Interview

### Logic Assumptions :

| # | Assumption | Reason |
|---|-----------|--------|
| 1 | Schema positions are 1-based inclusive (e.g., `name 1 20` means positions 1 through 20) | The sample solution uses `substring(start - 1, end)`, confirming 1-based inclusive positions |
| 2 | Single-position overlap is allowed and resolved by giving the boundary to the later field (e.g., `name 1 20` and `gender 20 21` — `NAME_END` becomes 19). Deep overlaps are rejected | The provided schema intentionally shares position 20. The boundary logically belongs to the later field. |
| 3 | Lines shorter than the last field's end position are skipped | Matches sample solution. Short lines would cause `IndexOutOfBoundsException` |
| 4 | All extracted fields are trimmed by default | Matches sample solution which calls `.trim()` on all fields. A per-field flag could be added if selective trimming is needed |
| 5 | First field must start at position 1 | Otherwise data at the beginning of the line is silently ignored, likely a schema error |
| 6 | Fields must be in sequential positional order | Out-of-order fields would produce incorrect constants in the generated code |
| 7 | No gaps allowed between fields | Gaps mean data at those positions is never extracted, likely a schema error |
| 8 | Duplicate field names (after camelCase conversion) not allowed | Would produce duplicate Java variables, causing compilation errors |
| 9 | Schema must have at least 3 columns per line (`name start end`), positive integers, `start <= end` | Minimum information needed to define a fixed-length field |
| 10 | Field names must be valid Java identifiers | Used directly as variable names in generated code |
| 11 | Underscores/hyphens converted to camelCase (e.g., `first_name` → `firstName`) | Follows Java naming conventions |
| 12 | Schema validation errors throw `SchemaParseException` with line number | Custom checked exception provides clear error handling instead of `return null` |

### Testing Assumptions :

| # | Assumption | Reason |
|---|-----------|--------|
| 1 | Valid and invalid test inputs were assumed based on the schema format and constraints | No test data specification was provided, so boundary and edge cases were derived from the schema rules |
| 2 | Tests for `parseSchema` use the actual `schema.txt` for valid cases and temporary files for invalid cases | Using the real schema ensures tests stay in sync. Temp files are needed for invalid cases since we can't put bad data in the real schema |
| 3 | Generated `FixedLengthParser.java` and `Record.java` are validated by checking the generated source code content | The generated files are not compiled as part of the project, so we verify correctness by asserting against the generated string output |
| 4 | `@MethodSource` with `Stream<Arguments>` is used instead of `@CsvSource` for parameterized tests | Test inputs contain multi-line strings (`\n`) and spaces that CSV cannot represent cleanly without awkward escaping |

### Test Strategy :

| Test File | What it tests | How |
|-----------|--------------|-----|
| `SchemaFieldTest` | Single field validation (name, start, end) | Parameterized valid/invalid inputs against the `SchemaField` constructor. Covers: null/empty/blank names, invalid identifiers, negative/zero positions, start > end |
| `MainTest` | Schema parsing and cross-field validation | Valid schemas: actual `schema.txt`, boundary cases (single char field, large positions, many fields, single-position overlap). Invalid schemas: temp files testing empty file, missing columns, non-integer positions, gaps, deep overlaps, duplicates (including after camelCase), out-of-order fields |
| `MainTest` | `toCamelCase` conversion | Parameterized: single word, underscore-separated, hyphen-separated, uppercase input |
| `MainTest` | Overlap constant adjustment | Verifies `ParserGenerator` adjusts the earlier field's end constant when a single-position overlap is detected |
| `FixedLengthParserTest` | Generated `FixedLengthParser.java` structure | Verifies constants (count + names), method signatures, imports, field extraction with trim, short line handling. All dynamically derived from `schema.txt` |
| `RecordTest` | Generated `Record.java` structure | Verifies fields (count + names), constructor (params + assignments), `toString` contains all fields. All dynamically derived from `schema.txt` |

### Known Limitations :
The generated `FixedLengthParser.java` logic must match the provided sample solution and cannot be modified. As a result, there is no error handling inside the generated parser — no field validation, no data type checking, no logging of skipped lines, and no per-record error reporting. If requirements allowed modifying the parser logic, these would be addressed.

### AI Tools :
AI tools (Claude) were used to assist with debugging, test case design, code review, and documentation during development.
