package banking;

import banking.constants.AccConst;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;


public class Account implements AccConst {

    // task description depicts the full account / card number
    // should have 16 characters (digits in fact), including:
    //  6 digits (=400000 right now) - IIN_STR - issuer identification number
    //  9 digits - CAN - customer account number
    //  1 digit - checksum - may be any right now
    // because Integer data type covers more then 9 digits,
    // I choose that type for account number (it may be changed in the future)
    // it is fast and easy to maintain...
    // CAUTION:
    // 'full' account number (of 16 digits length) can be stored as 'long'
    // but I'll use its 'String' representation only
    // the 'account number' is set once per it's lifetime

    private final int can;


    // checksum digit - in fact it should be generated rather then stored
    // but at the stage #1 of the project it can be anything
    // so it is easier to store some random 1-digit value.
    // the 'account number checksum' is given once per it's lifetime

    private final int cs;


    // four-digit number
    // pin is NOT final - it can be changed by user in the future!!!

    private int pin;


    // how much money the account holds

    private int balance;


    // internal variable used by Account() constructor
    private static final Random rnd = new Random();


    // we have to create unique account / card number for every new user
    // so we have to remember already issued one's

    private static final Set<Integer> alreadyGeneratedCanNumbers = new HashSet<>();


    Account() {
        int pretendingNumber;

        // let's generate unique (not generated / remembered yet) 'can' (customer account number)
        // meeting the criteria - it has to be integer in the range:
        // ACC_CAN_NO_MIN_BOUND..ACC_CAN_NO_MAX_BOUND (min inclusive, max exclusive)

        do {
            pretendingNumber = rnd.nextInt(ACC_CAN_NO_MAX_BOUND - ACC_CAN_NO_MIN_BOUND) + ACC_CAN_NO_MIN_BOUND;
        } while (alreadyGeneratedCanNumbers.contains(pretendingNumber));

        this.can = pretendingNumber;
        alreadyGeneratedCanNumbers.add(pretendingNumber);

        // the pin doesn't have to be unique
        this.pin = rnd.nextInt(PIN_NO_MAX_BOUND - PIN_NO_MIN_BOUND) + PIN_NO_MIN_BOUND;

        // initial account balance
        this.balance = 0;

        // cs is 1-digit number - at the stage #1 of the project it can have any 1-digit value
        //this.cs = rnd.nextInt((int) Math.pow(10, ACC_CHECKSUM_LEN));
        this.cs = generateCheckDigit(IIN_STR + getCanAsString());

    } // Account() constructor


    // public static boolean isStringOfDigits(String s)
    // checks if given String is not empty and contains only digits
    //
    public static boolean isStringOfDigits(String s) {
        if (s == null || "".equals(s) || "null".equals(s)) {
            return false;
        }

        if (!s.matches("^\\d+$")) {
            return false;
        }

        return true;
    } //public static int isStringOfDigits(String s)


    // public static boolean isStringOfDigitsOfLengthN(String s, int n)
    // checks if given String is 'String of digits' and has length 'n'
    // if (n < 1) returns 'false' without checking the String itself
    //
    public static boolean isStringOfDigitsOfLengthN(String s, int n) {
        if (n < 1) {
            return false;
        }

        if (!isStringOfDigits(s)) {
            return false;
        }

        return s.length() == n ? true : false;
    } // public static boolean isStringOfDigitsOfLengthN(String s, int n)


    // int generateLuhnValue(String noAsString, boolean hasCheckDigit){...};
    // generates the 'Luhn value' based on the 'Luhn formula' by performing the actions described in that algorithm
    // to the number given as String:
    // - drop the last 'check' digit from the given number (if present - see 'boolean hasCheckDigit' value)
    // - multiply 'odd' numbers by 2 - where 'odd' means: 'at the odd position in the string representing
    //   given number (if counted from '1' -  or multiply 'even' digits if counted from '0' - as in java)
    // - subtract 9 if the result of multiplication is '>9'
    // - add all resulting numbers
    // that resulting 'Luhn value' added to the last 'check digit' should give the number which 'mod 10' is 0 (zero)
    //
    // input parameters:
    //      - 'noAsString' - string made of digits only - representing some number
    //      - 'hasCheckDigit' - if 'true' - the last digit will be dropped from calculations,
    //                          if 'false' - will be included
    // returns
    //      - 'Luhn number' described above
    //      - '-1' if any error occurred (eg. given string is empty or contains characters other then digits etc.)
    //
    // the 'generateLuhnValue' method checks by the way,
    // if the given String 'noAsString' is proper representation of a number:
    //      not null, not empty, only digits,
    //      has length > 0, if hasCheckDigit == false
    //      has length > 1, if hasCheckDigit == true
    //
    private static int generateLuhnValue(String noAsString, boolean hasCheckDigit) {

        if (!isStringOfDigits(noAsString)) {
            return -1;
        }

        if (noAsString.length() < (hasCheckDigit ? 2 : 1)) {
            return -1;
        }

        // let's convert given String to int[]
        int[] digits = Arrays.stream(noAsString.split(""))
                .mapToInt(Integer::parseInt)
                .toArray();


        // according to 'hasCheckDigit' value we apply the Luhn formula to all digits - if hasCheckDigit == false
        // or to 'all but last' - if hasCheckDigit == true;
        // it is said in the task description to multiply every 'odd' (in the term of index value) number,
        // but that applies to indices counted from 1 - we count from 0 in java,
        // so we multiply 'even-indexed' digits only
        //
        return IntStream
                .range(0, hasCheckDigit ? noAsString.length() - 1 : noAsString.length())
                .map(i -> i % 2 == 0 ? digits[i] * 2 : digits[i])
                .map(i -> i > 9 ? i - 9 : i)
                .sum();

    } // private static int generateLuhnValue(String, boolean)


    // int generateCheckDigit(String noWithoutCheckDigit)
    // calculates 'check digit' according to 'Luhn formula'
    // for the number given as String (without check digit yet, of course)
    // returns:
    //  - int value of that 'check digit' if ok
    //  - '-1' if something goes wrong: eg. String is empty, has other symbols than digits etc...
    //
    static int generateCheckDigit(String noWithoutCheckDigit) {
        int luhnValueForString = generateLuhnValue(noWithoutCheckDigit, false);

        if (luhnValueForString == -1) {
            return -1;
        }

        return (luhnValueForString % 10 == 0) ? (0) : (10 - luhnValueForString % 10);

    } // static int generateCheckDigit(String)


    // boolean isValidLuhnNumber(String)
    // checks if the given String represents valid number according to 'Luhn formula'
    // returns:
    //  - 'true' - if the Luhn formula is satisfied,
    //  - 'false' - otherwise
    //
    public static boolean isValidLuhnNumber(String noWithCheckDigit) {
        // the generateLuhnValue method check by the way, if the given String 'noWithCheckDigit'
        // is proper representation of a number (not null, not empty, only digits etc)
        int luhnValueForString = generateLuhnValue(noWithCheckDigit, true);
        if (luhnValueForString == -1) {
            return false;
        }

        int cs = Integer.parseInt(noWithCheckDigit.substring(noWithCheckDigit.length() - 1));

        return (luhnValueForString + cs) % 10 == 0;

    } // static boolean isValidLuhnNumber(String)


    // public static boolean isValidCanNumber(String s){
    // checks if the given String represents valid CAN (customer account number)
    // such valid CAN number should:
    //  - be string of ACC_CAN_LEN digits (9 digits right now)
    //  - represent the number within the ACC_CAN_NO_MIN_BOUND (inclusive) .. ACC_CAN_NO_MAX_BOUND (exclusive) range
    //
    public static boolean isValidCanNumber(String s) {
        if (!isStringOfDigitsOfLengthN(s, ACC_CAN_LEN)) {
            return false;
        }

        int can_val = Integer.parseInt(s);

        return (can_val >= ACC_CAN_NO_MIN_BOUND && can_val < ACC_CAN_NO_MAX_BOUND) ? true : false;

    } // public static boolean isValidCanNumber(String s)


    // public static boolean isValidSimpleBankingAccNumber(String s){
    // checks if a given String contains valid Account number of our Simple Banking system
    // such valid Account number should:
    //  - be string of ACC_FULL_NUMBER_LEN digits (16 digits right now)
    //  - be valid number according to Luhn formula (it checks by the way the value of last 'check digit' too)
    //  - the prefix of ACC_IIN_LEN digits (6 right now) should be equal to IIN_STR ('400000' right now)
    //  - next ACC_CAN_LEN digits (9 right now) should represent valid valid CAN (customer account number)
    //    that means it should represent the number within
    //    the ACC_CAN_NO_MIN_BOUND (inclusive) .. ACC_CAN_NO_MAX_BOUND (exclusive) range
    //
    public static boolean isValidSimpleBankingAccNumber(String s) {
        if (!isStringOfDigitsOfLengthN(s, ACC_FULL_NUMBER_LEN)) {
            return false;
        }
        if (!isValidLuhnNumber(s)) {
            return false;
        }
        if (!s.startsWith(IIN_STR)) {
            return false;
        }

        return isValidCanNumber(s.substring(ACC_IIN_LEN, ACC_IIN_LEN + ACC_CAN_LEN)) ? true : false;

    } // public static boolean isValidSimpleBankingAccNumber(String s)


    public static boolean isValidSimpleBankingPinNumber(String s) {
        if (!isStringOfDigitsOfLengthN(s, PIN_LEN)) {
            return false;
        }

        int pin_val = Integer.parseInt(s);

        return (pin_val >= PIN_NO_MIN_BOUND && pin_val < PIN_NO_MAX_BOUND) ? true : false;

    } // public static boolean isValidSimpleBankingPinNumber(String s)


    // public static int extractCanNumberFromFullAccNo(String s)
    // extract as 'int' the CAN (customer account number) from given string
    // representing Simple Banking Full Account Number
    // checks if the given String represent valid Simple Banking Full Account Number
    // if no - returns '-1'
    //
    public static int extractCanNumberFromFullAccNo(String s) {
        if (!isValidSimpleBankingAccNumber(s)) {
            return -1;
        }

        return Integer.parseInt(s.substring(ACC_IIN_LEN, ACC_IIN_LEN + ACC_CAN_LEN));
    } // public static int extractCanNumberFromFullAccNo(String s)


    // static boolean updateIssuedAccList(String s)
    // checks if the given String represents valid 'SimpleBanking Account / Card Full Number'
    // if not, returns 'false'
    // if yes - adds the int representing its CAN (customer account number)
    // to the list (set in fact) of 'alreadyGeneratedCanNumbers' (runtime utilized data structure)
    // and returns 'true'
    // this function is intended to help ensuring that no CAN will be generated more then once
    // and all Acc / Card numbers already stored in external DB file will be unique
    //
    static boolean updateIssuedAccList(String s) {
        if (!isValidSimpleBankingAccNumber(s)) {
            return false;
        }

        alreadyGeneratedCanNumbers.add(extractCanNumberFromFullAccNo(s));

        return true;
    } // static boolean updateIssuedAccList(String s)


    // get Customer Account Number as 'int'
    int getCanAsNumber() {
        return can;
    }


    // get Customer Account Number as 'String'
    // returns zero-left-padding String of ACC_CAN_LEN
    // representing the CAN part of account / cart number
    String getCanAsString() {
        return String.format(ACC_CAN_FORMAT_STRING, can);
    }


    // returns String representing 16-digit 'full account number'
    String getFullNoAsString() {
        return String.format(ACC_FULL_NO_2ARG_FORMAT_STRING, can, cs);
    }


    int getPinAsNumber() {
        return pin;
    }


    String getPinAsString() {
        return String.format(ACC_PIN_FORMAT_STRING, pin);
    }


    // updates pin with given int value
    // returns:
    //  true - if pin was successfully updated
    //  false - if given value doesn't satisfy pin-related requirements
    //      (eg is < PIN_NO_MIN_BOUND or >= PIN_NO_MAX_BOUND)
    //      in that case the pin number is NOT updated
    // this method is not named 'setPin', because it has the return value...
    // and therefore is not 'proper setter' according to 'java beans convention'
    //
    boolean updatePin(int newPinAsInt) {
        if (newPinAsInt >= PIN_NO_MIN_BOUND && newPinAsInt < PIN_NO_MIN_BOUND) {
            pin = newPinAsInt;
            return true;
        } else {
            return false;
        }
    } // boolean updatePin(int)


    // updates pin with given String
    // returns:
    //   true - if pin was successfully updated
    //   false - if given 'newPinAsString' doesn't satisfy pin-related requirements:
    //      - it has to have 4-digits (no other characters / symbols)
    //      - it's integer representation has to be in the range:
    //      PIN_NO_MIN_BOUND..PIN_NO_MAX_BOUND (min inclusive, max exclusive)
    //   in that case the pin number is NOT updated
    // this method is not named 'setPin', because it has the return value...
    // and therefore is not 'proper setter' according to 'java beans convention'
    //
    boolean updatePin(String newPinAsString) {
        if (newPinAsString == null || !newPinAsString.matches("^\\d{" + PIN_LEN + "}$")) {
            return false;
        }
        return updatePin(Integer.parseInt(newPinAsString));
    } // boolean updatePin(String)


    int getBalance() {
        return balance;
    }


    void setBalance(int newBalance) {
        this.balance = newBalance;
    }


    // updates balance by given value (maybe 'in +' or 'in -')
    // returns new balance value
    //
    int updateBalanceBy(int changeBy) {
        this.balance += changeBy;
        return this.balance;
    } // int updateBalanceBy(int)


    public String toString() {
        return "Account{no=" + getFullNoAsString() + ", pin=" + getPinAsString() + ", balance=" + balance + "}";
    }

} // class Account
