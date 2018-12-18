package com.joklek.fakec;

import com.joklek.fakec.codegen.StringTable;

import java.util.List;
import java.util.Scanner;

@SuppressWarnings("squid:S1135")
public class Interpreter {

    private final StringTable strings;
    private boolean running;
    private final int codeBase;
    private int[] memory;
    private int ip; // instruction
    private int sp; // stack
    private int bp; // base

    Scanner scanner;

    public Interpreter(List<Integer> code, StringTable strings) {
        this.strings = strings;
        this.running = true;
        this.scanner = new Scanner(System.in);

        this.codeBase = 512;
        this.memory = new int[4096];
        this.ip = codeBase;
        this.sp = 2048;
        this.bp = sp;

        for (int i = 0; i < code.size(); i++){
            memory[codeBase + i] = code.get(i);
        }
    }

    public void execute(){
        while (running) {
            executeStep();
            /*System.out.println();
            System.out.println("STACK");
            for (int i = 2048; i < 2055; i++){
                System.out.println(i-2048 + ":" + i + ": " + memory[i]);
            }*/
        }
        //System.out.println("Program terminanted with " + memory[2048]);
    }

    private void executeStep() {
        int opcode = readCode();

        //InstructionResolver instructionResolver = new InstructionResolver();
        //System.out.println(instructionResolver.resolveInstruction(opcode));

        switch (opcode) {
            case 0x10: addInteger(); break;
            case 0x11: addFloat(); break;
            case 0x12: subInteger(); break;
            case 0x13: subFloat(); break;
            case 0x14: multInteger(); break;
            case 0x15: multFloat(); break;
            case 0x16: divInteger(); break;
            case 0x17: divFloat(); break;
            case 0x18: modulus(); break;

            case 0x20: equalsInteger(); break;
            case 0x21: equalsFloat(); break;
            case 0x22: lessInteger(); break;
            case 0x23: lessFloat(); break;
            case 0x24: lessOrEqualInteger(); break;
            case 0x25: lessOrEqualFloat(); break;
            case 0x26: greaterInteger(); break;
            case 0x27: greaterFloat(); break;
            case 0x28: greaterOrEqualInteger(); break;
            case 0x29: greaterOrEqualFloat(); break;

            case 0x30: and(); break;
            case 0x31: or(); break;
            case 0x32: not(); break;

            case 0x40: pop(); break;
            //case 0x41: popf(); break;
            case 0x45: instrPush(); break;
            case 0x46: instrPush(); break;

            case 0x50: peek(); break;
            case 0x51: poke(); break;
            case 0x52: alloc(); break;

            case 0x60: call(readCode()); break;
            case 0x61: ret(0); break;
            case 0x62: ret(pop()); break;
            case 0x63: exit(); break;
            case 0x64: jmp(); break;
            case 0x65: jmpz(); break;
            //case 0x66: error(); break;

            case 0x70: stdOutInteger(); break;
            case 0x71: stdOutFloat(); break;
            case 0x72: stdOutString(); break;
            case 0x73: stdOutChar(); break;
            case 0x74: stdOutBool(); break;
            case 0x75: stdin(); break;
            default:
                throw new UnsupportedOperationException(String.format("Unsupported instruction with code %03X %d", opcode, opcode));
        }
    }

    private void pushFloat() {

    }

    private void and() {
        int b = pop();
        int a = pop();
        boolean bb = b == 1;
        boolean aa = a == 1;

        push( aa && bb ? 1 : 0);
    }

    private void or() {
        int b = pop();
        int a = pop();
        boolean bb = b == 1;
        boolean aa = a == 1;

        push( aa || bb ? 1 : 0);
    }

    public void not(){
        int a = pop() == 1 ? 0 : 1;
        push(a);
    }

    public void alloc(){
        int num = readCode();
        sp += num;
    }

    public void jmp(){
        goTo(readCode());
    }

    public void jmpz(){
        int target = readCode();
        if (pop() == 0) {
            goTo(target);
        }
    }

    private void exit() {
        running = false;
    }

    private void call(int args) {
        sp -= args;
        int target = memory[sp - 3];

        memory[sp - 3] = ip;
        memory[sp - 2] = bp;
        memory[sp - 1] = sp - 3;

        goTo(target);
        bp = sp;
    }

    private void ret(int value) {
        int oldIp = memory[bp -3];
        int oldFp = memory[bp -2];
        int oldSp = memory[bp -1];

        ip = oldIp;
        bp = oldFp;
        sp = oldSp;
        push(value);
    }

    private void peek() {
        int index = readCode();
        int a = memory[bp +index];
        //System.out.println("peek at " + index + " with = " + a);
        push(a);
    }

    private void poke() {
        int index = readCode();
        memory[bp +index] = pop();
        //System.out.println("poke at " + index + " = " + memory[bp+index]);
    }

    public void instrPush(){
        push(readCode());
    }

    private void stdOutInteger() {
        int integer = pop();
        System.out.print(integer);
    }

    private void stdOutFloat() {
        float a = Float.intBitsToFloat(pop());
        System.out.println(a);
    }

    private void stdOutString() {
        int position = pop();
        System.out.print(strings.get(position));
    }

    private void stdOutChar() {
        char a = (char) pop();
        System.out.print(a);
    }

    private void stdOutBool() {
        int a = pop();
        String bool = a == 0 ? "false" : "true";
        System.out.print(bool);
    }

    private void stdin() {
        int n = 0;
        scanner.nextInt();
        push(n);
    }


    private void addInteger() {
        int b = pop();
        int a = pop();
        push(a + b);
    }

    private void addFloat() {
        /*float b =;
        float a=;
        push(a+b);*/
        // TODO
    }

    private void subInteger() {
        int b = pop();
        int a = pop();
        push(a - b);
    }

    private void subFloat() {
        // TODO
    }


    private void multInteger() {
        int b = pop();
        int a = pop();
        push(a * b);
    }

    private void multFloat() {
        // TODO
    }

    private void divInteger() {
        int b = pop();
        int a = pop();
        push(a / b);
    }

    private void divFloat() {
        // TODO
    }

    private void modulus() {
        int b = pop();
        int a = pop();
        push(a % b);
    }


    private void equalsInteger() {
        int b = pop();
        int a = pop();
        push(a == b ? 1 : 0);
    }

    private void equalsFloat() {
        // TODO
    }

    private void lessInteger() {
        int b = pop();
        int a = pop();
        push(a < b ? 1 : 0);
    }

    private void lessFloat() {
        // TODO
    }

    private void lessOrEqualInteger() {
        int b = pop();
        int a = pop();
        push(a <= b ? 1 : 0);
    }

    private void lessOrEqualFloat() {
        // TODO
    }

    private void greaterInteger() {
        int b = pop();
        int a = pop();
        push(a > b ? 1 : 0);
    }

    private void greaterFloat() {
        // TODO
    }

    private void greaterOrEqualInteger() {
        int b = pop();
        int a = pop();
        push(a >= b ? 1 : 0);
    }

    private void greaterOrEqualFloat() {
        // TODO
    }

    /*---------------------------------------------------------------------------------------------------------*/

    private int pop(){
        sp--;
        return memory[sp];
    }

    private void push(int value){
        memory[sp] = value;
        sp++;
    }

    private void goTo(int target){
        ip = target + codeBase;
    }

    private int readCode(){
        int result = memory[ip];
        ip++;
        return result;
    }
}
