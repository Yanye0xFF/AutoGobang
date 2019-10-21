#ifndef __MAIN_H__
#define __MAIN_H__

#include <windows.h>

/*  To use this exported function of dll, include this header
 *  in your project.
 */

#ifdef BUILD_DLL
    #define DLL_EXPORT __declspec(dllexport)
#else
    #define DLL_EXPORT __declspec(dllimport)
#endif


#ifdef __cplusplus
extern "C"
{
#endif

void DLL_EXPORT SomeFunction(const LPCSTR sometext);
void DLL_EXPORT init(int size);
void DLL_EXPORT restart();
void DLL_EXPORT takeBack();
int DLL_EXPORT begin();
int DLL_EXPORT getBestX();
int DLL_EXPORT getBestY();
int DLL_EXPORT turn(int x,int y);
int DLL_EXPORT setTimeoutTurn(int value);
int DLL_EXPORT setTimeoutMatch(int value);
void DLL_EXPORT close();
#ifdef __cplusplus
}
#endif

#endif // __MAIN_H__
