package com.ikhaydev.internal_audio_recorder

interface AudioCapture {

    /**
     * Before a capture session can be started, permission is checked
     * and called if not granted requestRecordAudioPermission().
     * Start capturing audio
     */
    fun startCapturing(outputPath: String, encoding: Int, sampleRate: Int): String?

    /**
     * Stop capture audio and save's file
     */
    fun stopCapturing()

    /**
     * Converts PCM File to M4A
     */
    fun convertPCMTOM4AFile(inputPath: String, outputPath: String, sampleRate: Int): String?

}