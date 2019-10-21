#include "main.h"
#include "AI.h"
#include <iostream>
#include <cstdlib>
#include <sstream>
#include <string>
#include <Windows.h>

static AI wine;
static Pos best,input;
// a sample exported function
void DLL_EXPORT SomeFunction(const LPCSTR sometext)
{
    MessageBoxA(0, sometext, "DLL Message", MB_OK | MB_ICONINFORMATION);
}


void init(int size){
    wine.SetSize(size);
}

void restart(){
    wine.ReStart();
}

void takeBack(){
    wine.DelMove();
}

int begin(){
    best = wine.TurnBest();
    wine.TurnMove(best);
    return 200;
}

int getBestX(){
    return best.x;
}

int getBestY(){
    return best.y;
}

int turn(int x,int y){
    input.x=x;input.y=y;
    wine.TurnMove(input);
    best = wine.TurnBest();
    wine.TurnMove(best);
    return 200;
}

int DLL_EXPORT setTimeoutTurn(int value) {
    if (value != 0) {
        wine.timeout_turn = value;
        return 200;
    }
    return 0;
}

int DLL_EXPORT setTimeoutMatch(int value) {
     if (value != 0) {
        wine.timeout_match = value;
        return 200;
     }
     return 0;
}

void DLL_EXPORT close() {
    exit(0);
}

extern "C" DLL_EXPORT BOOL APIENTRY DllMain(HINSTANCE hinstDLL, DWORD fdwReason, LPVOID lpvReserved)
{
    switch (fdwReason)
    {
        case DLL_PROCESS_ATTACH:
            // attach to process
            // return FALSE to fail DLL load
            break;

        case DLL_PROCESS_DETACH:
            // detach from process
            break;

        case DLL_THREAD_ATTACH:
            // attach to thread
            break;

        case DLL_THREAD_DETACH:
            // detach from thread
            break;
    }
    return TRUE; // succesful
}
