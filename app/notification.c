#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <unistd.h>

JNIEXPORT void JNICALL Java_com_example_autohelper_1pms_NotificationHelper_setNotification(JNIEnv *env, jobject obj, jstring dateStr) {
    const char *nativeDateStr = (*env)->GetStringUTFChars(env, dateStr, 0);

    struct tm tm;
    time_t t;
    memset(&tm, 0, sizeof(struct tm));
    strptime(nativeDateStr, "%d.%m.%Y", &tm);
    t = mktime(&tm);

    if (t == -1) {
        printf("Failed to parse date\n");
        return;
    }

    time_t now = time(NULL);
    double seconds = difftime(t, now);

    if (seconds <= 0) {
        printf("The date is in the past\n");
        return;
    }

    printf("Notification set for %s\n", nativeDateStr);
    sleep((unsigned int)seconds);

    // Get the class and method ID for the showNotification method
    jclass cls = (*env)->GetObjectClass(env, obj);
    jmethodID methodID = (*env)->GetMethodID(env, cls, "showNotification", "(Ljava/lang/String;Ljava/lang/String;)V");

    if (methodID == NULL) {
        printf("Failed to find method\n");
        return;
    }

    // Call the showNotification method
    jstring title = (*env)->NewStringUTF(env, "Notification");
    jstring message = (*env)->NewStringUTF(env, nativeDateStr);
    (*env)->CallVoidMethod(env, obj, methodID, title, message);

    (*env)->DeleteLocalRef(env, title);
    (*env)->DeleteLocalRef(env, message);
    (*env)->ReleaseStringUTFChars(env, dateStr, nativeDateStr);
}