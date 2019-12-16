#include <jni.h>
#include <string>
#include <iostream>
#include <opencv2/opencv.hpp>
#include <EGL/egl.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <android/log.h>
using namespace cv;
using namespace std;

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_com_km_cdw_androidopencvwebcam_MainActivity_stringFromJNI(
            JNIEnv *env,
            jobject /* this */) {
        std::string hello = "Hello from C++";
        return env->NewStringUTF(hello.c_str());
    }

    JNIEXPORT jboolean JNICALL
    Java_com_km_cdw_androidopencvwebcam_ImageProcess_CVProcess_OpencvNativeManager_convertYUV2RGB(
            JNIEnv *env, jobject thiz, jint w, jint h, jbyteArray src_bytes, jintArray dst_ints) {
        // TODO: implement convertYUV2RGB()
        jbyte *_yuv = env->GetByteArrayElements(src_bytes, 0);
        jint *_bgra = env->GetIntArrayElements(dst_ints, 0);

        Mat m_yuv(h + h / 2, w, CV_8UC1, (unsigned char *) _yuv);
        Mat m_bgra(h, w, CV_8UC4, (unsigned char *) _bgra);

        cvtColor(m_yuv, m_bgra, COLOR_YUV420sp2BGR, 4);


        env->ReleaseByteArrayElements(src_bytes, _yuv, 0);
        env->ReleaseIntArrayElements(dst_ints, _bgra, 0);

        return true;

    }

    JNIEXPORT jboolean JNICALL
    Java_com_km_cdw_androidopencvwebcam_ImageProcess_CVProcess_OpencvNativeManager_convertRGB2YUV(
            JNIEnv *env, jobject thiz, jint w, jint h, jintArray src_ints, jbyteArray dst_bytes) {
        // TODO: implement convertRGB2YUV()
        jint *_bgra = env->GetIntArrayElements(src_ints, 0);
        jbyte *_yuv = env->GetByteArrayElements(dst_bytes, 0);

        Mat m_bgra(h, w, CV_8UC4, (unsigned char *) _bgra);
        Mat m_yuv(h + h / 2, w, CV_8UC1, (unsigned char *) _yuv);

        cvtColor(m_bgra, m_yuv, COLOR_BGRA2YUV_YV12, 1);

        env->ReleaseIntArrayElements(src_ints, _bgra, 0);
        env->ReleaseByteArrayElements(dst_bytes, _yuv, 0);


        return true;
    }

    JNIEXPORT jboolean JNICALL
    Java_com_km_cdw_androidopencvwebcam_ImageProcess_CVProcess_OpencvNativeManager_processEmbose(
            JNIEnv *env, jobject thiz, jint w, jint h, jbyteArray src_bytes, jbyteArray dst_bytes) {
        // TODO: implement processEmbose()
        jbyte *_src = env->GetByteArrayElements(src_bytes, 0);
        jbyte *_dst = env->GetByteArrayElements(dst_bytes, 0);

        Mat src_mat(h * 3 / 2, w, CV_8UC1, (unsigned char *) _src);
        Mat dst_mat(h * 3 / 2, w, CV_8UC1, (unsigned char *) _dst);

        Mat mask(3, 3, CV_32F, Scalar(0));
        mask.at<float>(0, 0) = -1.0;
        mask.at<float>(2, 2) = 1.0;

        filter2D(src_mat, dst_mat, CV_16S, mask);
        dst_mat.convertTo(dst_mat, CV_8U, 1, 128);

        env->ReleaseByteArrayElements(src_bytes, _src, 0);
        env->ReleaseByteArrayElements(dst_bytes, _dst, 0);

        return true;
    }

    JNIEXPORT jboolean JNICALL
    Java_com_km_cdw_androidopencvwebcam_ImageProcess_CVProcess_OpencvNativeManager_convertRGB2eachYUV(
            JNIEnv *env, jobject thiz, jint w, jint h, jintArray src_ints, jbyteArray y_bytes,
            jbyteArray u_bytes, jbyteArray v_bytes) {
        // TODO: implement convertRGB2eachYUV()
        jint *_bgra = env->GetIntArrayElements(src_ints, 0);
        jbyte *_y = env->GetByteArrayElements(y_bytes, 0);
        jbyte *_u = env->GetByteArrayElements(u_bytes, 0);
        jbyte *_v = env->GetByteArrayElements(v_bytes, 0);

    //    Mat yuv_mat[3];

        Mat yMat(h, w, CV_8UC1, (unsigned char *) _y);
        Mat uMat(h, w, CV_8UC1, (unsigned char *) _u);
        Mat vMat(h, w, CV_8UC1, (unsigned char *) _v);
        vector<Mat> yuvMat;
        yuvMat.push_back(yMat);
        yuvMat.push_back(uMat);
        yuvMat.push_back(vMat);

    //    char* y_ptr = yuv_mat[0].ptr<char>();
    //    char* u_ptr = yuv_mat[1].ptr<char>();
    //    char* v_ptr = yuv_mat[2].ptr<char>();
    //
    //    memcpy((void*)y_ptr, (void*)_y, (size_t)w*h);
    //    memcpy((void*)u_ptr, (void*)_u, (size_t)w*h);
    //    memcpy((void*)v_ptr, (void*)_v, (size_t)w*h);

        Mat m_yuv(h, w, CV_8UC3);
        Mat m_bgra(h, w, CV_8UC4, (unsigned char *) _bgra);

        cvtColor(m_bgra, m_yuv, COLOR_BGRA2YUV_YV12, 3);
        split(m_yuv, yuvMat);

        env->ReleaseIntArrayElements(src_ints, _bgra, 0);
        env->ReleaseByteArrayElements(y_bytes, _y, 0);
        env->ReleaseByteArrayElements(u_bytes, _u, 0);
        env->ReleaseByteArrayElements(v_bytes, _v, 0);

        return true;

    }

    JNIEXPORT jboolean JNICALL
    Java_com_km_cdw_androidopencvwebcam_ImageProcess_CVProcess_OpencvNativeManager_convertEachYUV2RGB(
            JNIEnv *env, jobject thiz, jint w, jint h, jbyteArray y_bytes,
            jbyteArray u_bytes, jbyteArray v_bytes, jintArray dst_ints) {
        // TODO: implement convertRGB2eachYUV()
        jint *_bgra = env->GetIntArrayElements(dst_ints, 0);
        jbyte *_y = env->GetByteArrayElements(y_bytes, 0);
        jbyte *_u = env->GetByteArrayElements(u_bytes, 0);
        jbyte *_v = env->GetByteArrayElements(v_bytes, 0);

    //    Mat yuv_mat[3];

        Mat yMat(h, w, CV_8UC1, (unsigned char *) _y);
        Mat uMat(h, w, CV_8UC1, (unsigned char *) _u);
        Mat vMat(h, w, CV_8UC1, (unsigned char *) _v);
        vector<Mat> yuvMat;
        yuvMat.push_back(yMat);
        yuvMat.push_back(uMat);
        yuvMat.push_back(vMat);

    //    char* y_ptr = yuv_mat[0].ptr<char>();
    //    char* u_ptr = yuv_mat[1].ptr<char>();
    //    char* v_ptr = yuv_mat[2].ptr<char>();
    //
    //    memcpy((void*)y_ptr, (void*)_y, (size_t)w*h);
    //    memcpy((void*)u_ptr, (void*)_u, (size_t)w*h);
    //    memcpy((void*)v_ptr, (void*)_v, (size_t)w*h);

        Mat m_yuv(h, w, CV_8UC3);
        merge(yuvMat, m_yuv);
        Mat m_bgra(h, w, CV_8UC4, (unsigned char *) _bgra);

        cvtColor(m_yuv, m_bgra, COLOR_YUV420sp2BGR);

        env->ReleaseIntArrayElements(dst_ints, _bgra, 0);
        env->ReleaseByteArrayElements(y_bytes, _y, 0);
        env->ReleaseByteArrayElements(u_bytes, _u, 0);
        env->ReleaseByteArrayElements(v_bytes, _v, 0);

        return true;

    }
    //extern "C"
    //JNIEXPORT jboolean JNICALL
    //Java_com_km_cdw_androidopencvwebcam_ImageProcess_CVProcess_OpencvNativeManager_convertYUV2RGB2(
    //        JNIEnv *env, jobject thiz, jint w, jint h, jbyteArray src_bytes, jbyteArray dst_bytes) {
    //    // TODO: implement convertYUV2RGB2()
    //    jbyte* _yuv  = env->GetByteArrayElements(src_bytes, 0);
    //    jint*  _bgra = env->GetByteArrayElements(dst_ints, 0);
    //
    //    Mat m_yuv(h + h/2, w, CV_8UC1, (unsigned char*)_yuv);
    //    Mat m_bgra(h, w, CV_8UC4, (unsigned char*)_bgra);
    //
    //    cvtColor(m_yuv, m_bgra, COLOR_YUV420sp2BGR, 4);
    //
    //    env->ReleaseByteArrayElements(src_bytes, _yuv, 0);
    //    env->ReleaseIntArrayElements(dst_ints, _bgra, 0);
    //
    //    return true;
    //}extern "C"
    //JNIEXPORT jboolean JNICALL
    //Java_com_km_cdw_androidopencvwebcam_ImageProcess_CVProcess_OpencvNativeManager_convertRGB2YUV2(
    //        JNIEnv *env, jobject thiz, jint w, jint h, jbyteArray src_bytes, jbyteArray dst_bytes) {
    //    // TODO: implement convertRGB2YUV2()
    //}

    JNIEXPORT jboolean JNICALL
    Java_com_km_cdw_androidopencvwebcam_ImageProcess_CVProcess_OpencvNativeManager_convertEachYUVsp2RGB(
            JNIEnv *env, jobject thiz, jint w, jint h, jbyteArray y_bytes, jbyteArray u_bytes,
            jintArray dst_ints) {
        // TODO: implement convertEachYUVsp2RGB()
        jint *_bgra = env->GetIntArrayElements(dst_ints, 0);
        jbyte *_y = env->GetByteArrayElements(y_bytes, 0);
        jbyte *_u = env->GetByteArrayElements(u_bytes, 0);

    //    Mat yuv_mat[3];

        Mat yMat(h, w, CV_8UC1, (unsigned char *) _y);
        Mat uMat(h, w, CV_8UC1, (unsigned char *) _u);
        vector<Mat> yuvMat;
        yuvMat.push_back(yMat);
        yuvMat.push_back(uMat);
    //    char* y_ptr = yuv_mat[0].ptr<char>();
    //    char* u_ptr = yuv_mat[1].ptr<char>();
    //    char* v_ptr = yuv_mat[2].ptr<char>();
    //
    //    memcpy((void*)y_ptr, (void*)_y, (size_t)w*h);
    //    memcpy((void*)u_ptr, (void*)_u, (size_t)w*h);
    //    memcpy((void*)v_ptr, (void*)_v, (size_t)w*h);

        Mat m_yuv(h, w, CV_8UC1);
        merge(yuvMat, m_yuv);
        Mat m_bgra(h, w, CV_8UC4, (unsigned char *) _bgra);

        cvtColor(m_yuv, m_bgra, COLOR_YUV420sp2BGR);

        env->ReleaseIntArrayElements(dst_ints, _bgra, 0);
        env->ReleaseByteArrayElements(y_bytes, _y, 0);
        env->ReleaseByteArrayElements(u_bytes, _u, 0);

        return true;
    }

    JNIEXPORT jboolean JNICALL
    Java_com_km_cdw_androidopencvwebcam_ImageProcess_GLRendering_Renderer_GLSurfaceRenderer_1RgbMat_drawMatNative(
            JNIEnv *env, jobject thiz, jint w, jint h, jlong mat_addr, jint tex_id) {
        // TODO: implement drawMatNative()
        Mat& mat_input = *(Mat*) mat_addr;
        glBindTexture(GL_TEXTURE_2D, (GLuint)tex_id);
        glTexImage2D(GL_TEXTURE_2D,
                     0,
                     GL_RGB,
                     w, h,
                     0,
                     GL_RGB,
                     GL_UNSIGNED_BYTE,
                     mat_input.ptr());

        return true;
    }
}
