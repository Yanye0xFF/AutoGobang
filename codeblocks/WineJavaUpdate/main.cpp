#include "main.h"
#include "AIWine.h"
#include "ChessShape.h"

AIWine* ai= new AIWine();
int aiX=0, aiY=0;

// a sample exported function
void DLL_EXPORT SomeFunction(const LPCSTR sometext)
{
    MessageBoxA(0, sometext, "DLL Message", MB_OK | MB_ICONINFORMATION);
}

void DLL_EXPORT init(int size){
    ai->setSize(size);
}

void DLL_EXPORT restart(){
    ai->restart();
}

void DLL_EXPORT takeBack(){
    ai->turnUndo();
}

int DLL_EXPORT begin(){
    ai->turnBest(aiX, aiY);
    return 200;
}

int DLL_EXPORT getBestX(){
    return aiX;
}
int DLL_EXPORT getBestY(){
    return aiY;
}

int DLL_EXPORT turn(int x,int y){
    if (ai->isValidPos(x, y)) {
        ai->turnMove(x, y);
        ai->turnBest(aiX, aiY);
        return 200;
    }
    return 0;
}

int DLL_EXPORT setTimeoutTurn(int value){
    if (value != 0){
        ai->timeout_turn = value;
        return 200;
    }
    return 0;
}

int DLL_EXPORT setTimeoutMatch(int value){
    if (value != 0){
        ai->timeout_match = value;
        return 200;
    }
    return 0;
}

void DLL_EXPORT close(){
    delete ai;
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
