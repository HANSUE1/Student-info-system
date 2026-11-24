# Registration Process — Formatting Guidelines

**Platform:** Student-info-system (Android, Java)  
**Scope:** Every screen that registers or edits a user/account (`RegisterActivity`, `StudentFormActivity`, and any future registration screen).  
**Owner:** All contributors touching registration forms.

These rules standardize data entry, field validation, error messaging, and syntax so every registration behaves and looks the same.

---

## 1. Core design rule (non-negotiable)

Every registration screen must:

1. Validate **all** fields on submit and show **every** error at once (never stop at the first error).
2. Clear each field's error as soon as the user edits it.
3. Move focus to the first invalid field and shake it so the user sees where to look.
4. Never store data unless validation passes; registration is "design only" unless a storage layer is explicitly added.

The reference implementation is the `&`-chained validation in `RegisterActivity.register()` and `StudentFormActivity.save()`:

```java
boolean valid = Forms.show(fieldA, Validator.ruleA(Forms.text(fieldA)))
        & Forms.show(fieldB, Validator.ruleB(Forms.text(fieldB)))   // '&' (bitwise), NOT '&&'
        & Forms.show(fieldC, Validator.ruleC(...));                 // every error is shown
if (!valid) {
    Forms.focusFirstError(allFields());
    return;
}
// proceed only when every field is valid
```

**Why `&` instead of `&&`:** `&&` short-circuits, so only the first error would appear. `&` evaluates every `Forms.show(...)` call, surfacing all errors at once.

---

## 2. Field registry (standard fields, syntax, and limits)

| Field | Input type | Required? | Syntax / pattern | Length | Example | Error message |
|-------|-----------|-----------|------------------|--------|---------|---------------|
| Username | text | Yes | `[A-Za-z0-9._]+` (letters, digits, `.`, `_` only; no spaces) | 4–20 | `juan_dc` | `Enter a username` / `Use 4 to 20 characters` / `Use letters, numbers, . or _ only (no spaces)` |
| Password | textPassword | Yes | At least one letter **and** at least one digit | ≥ 8 | `secret123` | `Enter a password` / `Use at least 8 characters` / `Include at least one letter and one number` |
| Confirm password | textPassword | Yes | Must equal the password field | ≥ 8 | `secret123` | `Re-enter your password` / `Passwords do not match` |
| Student number | text | Yes | `YYYY-NNNN(NN)` — 4-digit year, dash, 4–6 digits | fixed | `2025-00123` | `Enter the student number` / `Use the format YYYY-NNNNN, e.g. 2025-00123` |
| Last name | text | Yes | Unicode letters, spaces, `-`, `'`, `.`; must contain ≥ 1 letter | ≤ 50 | `Dela Cruz` | `Enter the last name` / `Keep it under 50 characters` / `Use letters only (spaces, - ' and . are allowed)` |
| First name | text | Yes | same as last name | ≤ 50 | `Ma. Lourdes` | same as last name |
| Middle name | text | No | same as last name | ≤ 50 | `O'Neil-Peña` | `Keep it under 50 characters` / `Use letters only ...` (empty is valid) |
| Year level | dropdown/text | Yes | non-empty selection | — | `3rd Year` | `Select a year level` |
| Specialization | text | No | any text | ≤ 60 | `Computer Science` | `Keep it under 60 characters` (empty is valid) |

### Student number (`yyyy-#######`) — canonical syntax

The platform identifier `yyyy-#######` maps to the student number pattern:

```
^\d{4}-\d{4,6}$
```

- 4 digits (calendar/enrolment year), a literal hyphen `-`, then 4–6 digits.
- Always include the hyphen. No letters, no spaces, no leading/trailing whitespace.
- Valid: `2025-00123`, `2025-1234`, `2026-000001`
- Invalid: `202500123` (missing hyphen), `25-00123` (2-digit year), `2025-ABCDE` (letters), ` 2025-00123 ` (whitespace unless trimmed first)

> All field text is trimmed before validation (`Forms.text`); passwords are read raw (`Forms.raw`) so surrounding spaces inside a password are preserved.

---

## 3. Validation API (`Validator.java`)

Add new rules only in `com.example.myapplication.Validator`. Each rule returns `null` when valid, or the user-facing error string when invalid. Keep the class pure Java (no Android imports) so rules stay unit-testable in `ValidatorTest`.

Convention for every new rule:

```java
public static String fieldName(String s, ...) {
    if (s.isEmpty()) return "Enter the <field>";
    // syntax / length / content checks ...
    return "<specific, actionable message>";
    // return null when valid
}
```

Existing rules: `required`, `username`, `newPassword`, `confirmPassword`, `studentNumber`, `name`, `yearLevel`, `specialization`.

### Required vs optional semantics
- Required empty field → `"Enter the <field>"`.
- Optional empty field → `null` (valid).
- Optional non-empty field → still runs syntax/length checks.

---

## 4. UI syntax rules (XML + activity)

### XML layout (Material3)
- Wrap every field in `com.google.android.material.textfield.TextInputLayout` with `style="@style/Widget.Material3.TextInputLayout.OutlinedBox"`.
- Always pair with a `TextInputEditText` inside the layout.
- Set `app:counterEnabled="true"` and `app:counterMaxLength="<max>"` on length-limited fields.
- Set `app:helperText` to the exact same rule text shown in validation errors (keeps hint and error consistent).
- Set `android:hint` to the field label.
- Use `android:autofillHints` (`username`, `newPassword`, etc.) and matching `android:inputType`.
- Password fields use `app:endIconMode="password_toggle"`.
- IME flow: `actionNext` on all fields except the last, which uses `actionDone` and triggers submit.

### Activity/controller
1. Bind fields with `findViewById` in `onCreate`.
2. Call `Forms.clearErrorOnEdit(field1, field2, ...)` so errors disappear on edit.
3. On submit button click (and on `IME_ACTION_DONE` of the last field), call the submit method.
4. In the submit method, run the `&`-chained `Forms.show(...)` validation, then `Forms.focusFirstError(...)` on failure.
5. On success, return `setResult(RESULT_OK, ...)` and `finish()`, or persist via the storage layer if one exists.
6. Override `finish()` to call `Motion.back(this)` for the standard back animation.

### Error messaging
- Errors are set via `TextInputLayout.setError(...)` and cleared via `setError(null)`.
- Messages must be specific and actionable (tell the user what to type, not just "invalid").
- Never hardcode different wording for the same rule across screens — reuse `Validator` messages.

---

## 5. Template for a new registration screen

**`activity_<name>.xml`** — copy the field block pattern:

```xml
<com.google.android.material.textfield.TextInputLayout
    android:id="@+id/field_<name>"
    style="@style/Widget.Material3.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="<Label>"
    app:counterEnabled="true"
    app:counterMaxLength="<max>"
    app:helperText="<helper matching the Validator error>"
    app:startIconDrawable="@drawable/ic_<icon>">

    <com.google.android.material.textfield.TextInputEditText
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:autofillHints="<hint>"
        android:imeOptions="actionNext"
        android:inputType="<type>" />
</com.google.android.material.textfield.TextInputLayout>
```

**`<Name>Activity.java`:**

```java
public class <Name>Activity extends AppCompatActivity {
    private TextInputLayout fieldA;
    private TextInputLayout fieldB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_<name>);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // toolbar + back navigation
        fieldA = findViewById(R.id.field_a);
        fieldB = findViewById(R.id.field_b);
        Forms.clearErrorOnEdit(fieldA, fieldB);

        findViewById(R.id.btn_submit).setOnClickListener(v -> submit());
        // last field: actionDone -> submit()
    }

    private void submit() {
        boolean valid = Forms.show(fieldA, Validator.ruleA(Forms.text(fieldA)))
                & Forms.show(fieldB, Validator.ruleB(Forms.text(fieldB)));
        if (!valid) {
            Forms.focusFirstError(fieldA, fieldB);
            return;
        }
        setResult(RESULT_OK, ...);
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        Motion.back(this);
    }
}
```

**Validator addition:**

```java
public static String ruleA(String s) {
    if (s.isEmpty()) return "Enter the <field>";
    if (!<PATTERN>.matcher(s).matches()) return "<syntax message>";
    if (s.length() > <max>) return "Keep it under <max> characters";
    return null;
}
```

**ValidatorTest addition:**

```java
@Test
public void ruleA() {
    assertNull(Validator.ruleA("<valid example>"));
    assertEquals("Enter the <field>", Validator.ruleA(""));
    assertNotNull(Validator.ruleA("<invalid syntax>"));
    assertNotNull(Validator.ruleA("<too long>"));
}
```

---

## 6. Checklist before committing any registration change

- [ ] New field rules live in `Validator` (pure Java), not inline in the activity.
- [ ] Submit uses `&`-chained `Forms.show(...)` so all errors show together.
- [ ] `Forms.focusFirstError(...)` runs on failure.
- [ ] `Forms.clearErrorOnEdit(...)` is wired for every field.
- [ ] XML uses Material3 `TextInputLayout` + `TextInputEditText`, with matching `helperText` and `counterMaxLength`.
- [ ] Student numbers follow `^\d{4}-\d{4,6}$` (e.g. `2025-00123`).
- [ ] New rules are covered in `ValidatorTest` with valid, empty, syntax-invalid, and length-invalid cases.
- [ ] Error wording matches existing `Validator` messages for the same rule.

---

### Reference files
- `app/src/main/java/com/example/myapplication/Validator.java` — validation rules + regex
- `app/src/main/java/com/example/myapplication/Forms.java` — field read/error/focus helpers
- `app/src/main/java/com/example/myapplication/RegisterActivity.java` — account registration reference
- `app/src/main/java/com/example/myapplication/StudentFormActivity.java` — student registration reference (uses student number `YYYY-NNNNN`)
- `app/src/test/java/com/example/myapplication/ValidatorTest.java` — expected rule behavior
- `app/src/main/res/layout/activity_register.xml` — register layout reference
