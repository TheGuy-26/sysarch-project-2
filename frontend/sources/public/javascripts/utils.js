
const decToHexLength = (int, length) => '0x' + int.toString(16).padStart(length, '0');
const decToHex = int => '0x' + int.toString(16).padStart(8, '0');

function registerNameFromNumber(number) {
    if (number == 0) return "zero";
    if (number == 1) return "ra";
    if (number == 2) return "sp";
    if (number == 3) return "gp";
    if (number == 4) return "tp";
    if (number == 5) return "t0";
    if (number == 6) return "t1";
    if (number == 7) return "t2";
    if (number == 8) return "s0";
    if (number == 9) return "s1";
    if (number == 10) return "a0";
    if (number == 11) return "a1";
    if (number == 12) return "a2";
    if (number == 13) return "a3";
    if (number == 14) return "a4";
    if (number == 15) return "a5";
    if (number == 16) return "a6";
    if (number == 17) return "a7";
    if (number == 18) return "s2";
    if (number == 19) return "s3";
    if (number == 20) return "s4";
    if (number == 21) return "s5";
    if (number == 22) return "s6";
    if (number == 23) return "s7";
    if (number == 24) return "s8";
    if (number == 25) return "s9";
    if (number == 26) return "s10";
    if (number == 27) return "s11";
    if (number == 28) return "t3";
    if (number == 29) return "t4";
    if (number == 30) return "t5";
    if (number == 31) return "t6";
    return "unknown";
}

function csrAddressToName(address) {
//    if (address == 0xC00) return "cycle"     ;
//    if (address == 0xC01) return "time"      ;
//    if (address == 0xC02) return "instret"   ;
//    if (address == 0xC80) return "cycleh"    ;
//    if (address == 0xC81) return "timeh"     ;
//    if (address == 0xC82) return "instreth"  ;

    if (address == 0xF11) return "mvendorid" ;
    if (address == 0xF12) return "marchid"   ;
    if (address == 0xF13) return "mimpid"    ;
    if (address == 0xF14) return "mhartid"   ;
    if (address == 0xF15) return "mconfigptr" ;

    if (address == 0x300) return "mstatus"   ;
    if (address == 0x301) return "misa"      ;
    if (address == 0x302) return "medeleg"   ;
    if (address == 0x303) return "mideleg"   ;
    if (address == 0x304) return "mie"       ;
    if (address == 0x305) return "mtvec"     ;
    if (address == 0x306) return "mcounteren" ;
    if (address == 0x310) return "mstatush" ;
    if (address == 0x312) return "medelegh" ;

    if (address == 0x340) return "mscratch"  ;
    if (address == 0x341) return "mepc"      ;
    if (address == 0x342) return "mcause"    ;
    if (address == 0x343) return "mtval"     ;
    if (address == 0x344) return "mip"       ;
    if (address == 0x34A) return "mtinst"    ;
    if (address == 0x34B) return "mtval2"    ;

    if (address == 0xB00) return "mcycle"    ;
    if (address == 0xB02) return "minstret"  ;
    if (address == 0xB80) return "mcycleh"   ;
    if (address == 0xB82) return "minstreth" ;
    if (address == 0x320) return "mcountinhibit" ;
    return null;
}