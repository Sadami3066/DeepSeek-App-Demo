package com.opends.chat.design.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp

object OfficialIcons {
    val checkRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "check_regular",
            defaultWidth = 9.dp,
            defaultHeight = 10.dp,
            viewportWidth = 9f,
            viewportHeight = 10f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M0.723,4.693C0.996,4.471 1.398,4.512 1.62,4.785L4.057,7.785C4.279,8.059 4.238,8.46 3.965,8.682C3.691,8.904 3.29,8.863 3.068,8.589L0.63,5.589C0.408,5.316 0.45,4.915 0.723,4.693Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M3.213,8.721C2.918,8.527 2.836,8.132 3.03,7.838L6.967,1.838C7.16,1.543 7.555,1.461 7.85,1.655C8.144,1.848 8.226,2.243 8.033,2.537L4.095,8.537C3.902,8.832 3.507,8.914 3.213,8.721Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val crop: ImageVector by lazy {
        ImageVector.Builder(
            name = "crop",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M14.375,4.876C14.789,4.876 15.125,5.212 15.125,5.626V18.126L13.625,18.125V6.375H1.875V4.875L14.375,4.876Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M5.625,1.875V14.375H18.125").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Round,
            )
        }.build()
    }
    val addCloseRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_add_close_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M4.222,4.222C4.652,3.793 5.348,3.793 5.778,4.222L12.071,10.516L18.364,4.222C18.794,3.793 19.49,3.793 19.92,4.222C20.35,4.652 20.35,5.348 19.92,5.778L13.627,12.071L19.92,18.364C20.349,18.794 20.349,19.49 19.92,19.92C19.49,20.35 18.794,20.35 18.364,19.92L12.071,13.627L5.778,19.92C5.348,20.35 4.652,20.35 4.222,19.92C3.793,19.49 3.793,18.794 4.222,18.364L10.515,12.071L4.222,5.778C3.793,5.348 3.793,4.652 4.222,4.222Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val addOutline26: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_add_outline_26",
            defaultWidth = 26.dp,
            defaultHeight = 26.dp,
            viewportWidth = 26f,
            viewportHeight = 26f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M7.727,13H18.277").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 1.75f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M12.996,18.273L12.996,7.723").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 1.75f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M13.001,12.999m-11.331,0a11.331,11.331 0,1 1,22.662 0a11.331,11.331 0,1 1,-22.662 0").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val arrowRightOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_arrow_right_outline_20",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M9.072,11.76L11.559,9.273C12.09,8.743 12.355,8.477 12.406,8.157C12.422,8.053 12.422,7.947 12.406,7.844C12.355,7.523 12.09,7.258 11.559,6.727L9.072,4.24").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF7E7E7E)),
                strokeLineWidth = 1.3f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M2.955,8.005L12.176,8.005").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF7E7E7E)),
                strokeLineWidth = 1.3f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val authPhone: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_auth_phone",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13.75,0C14.347,0 14.919,0.237 15.341,0.659C15.763,1.081 16,1.653 16,2.25V17.75C16,18.347 15.763,18.919 15.341,19.341C14.919,19.763 14.347,20 13.75,20H6.25C5.653,20 5.081,19.763 4.659,19.341C4.237,18.919 4,18.347 4,17.75V2.25C4,1.653 4.237,1.081 4.659,0.659C5.081,0.237 5.653,0 6.25,0H13.75ZM13.75,1.5H6.25C6.051,1.5 5.86,1.579 5.72,1.72C5.579,1.86 5.5,2.051 5.5,2.25V17.75C5.5,18.164 5.836,18.5 6.25,18.5H13.75C13.949,18.5 14.14,18.421 14.28,18.28C14.421,18.14 14.5,17.949 14.5,17.75V2.25C14.5,2.051 14.421,1.86 14.28,1.72C14.14,1.579 13.949,1.5 13.75,1.5ZM11.249,15.5C11.448,15.5 11.639,15.578 11.78,15.719C11.92,15.859 12,16.05 12,16.249C12,16.448 11.922,16.639 11.781,16.78C11.641,16.92 11.45,17 11.251,17L8.751,17.004C8.552,17.004 8.361,16.926 8.22,16.785C8.08,16.645 8,16.454 8,16.255C8,16.056 8.078,15.865 8.219,15.724C8.359,15.583 8.55,15.504 8.749,15.504L11.249,15.5Z").toNodes(),
                fill = SolidColor(Color(0xFFA3A3A3)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val branchOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_branch_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M16.051,2.076C17.262,2.076 18.244,3.059 18.244,4.27C18.244,5.481 17.262,6.463 16.051,6.463C15.137,6.463 14.353,5.904 14.024,5.109H5.804C6.282,5.587 6.679,6.154 6.968,6.793L8.985,11.248C9.907,13.282 11.877,14.624 14.087,14.755C14.446,14.034 15.19,13.537 16.051,13.537C17.262,13.537 18.244,14.519 18.244,15.73C18.243,16.941 17.262,17.923 16.051,17.923C15.085,17.923 14.266,17.298 13.974,16.431C11.15,16.258 8.634,14.54 7.457,11.94L5.439,7.485C4.784,6.038 3.344,5.109 1.756,5.109V3.431H14.024C14.353,2.636 15.137,2.076 16.051,2.076ZM16.051,15.116C15.712,15.116 15.437,15.391 15.437,15.73C15.437,16.069 15.712,16.344 16.051,16.344C16.389,16.344 16.665,16.069 16.665,15.73C16.665,15.391 16.39,15.116 16.051,15.116ZM16.051,3.655C15.712,3.655 15.436,3.931 15.436,4.27C15.436,4.609 15.712,4.884 16.051,4.884C16.39,4.884 16.665,4.609 16.665,4.27C16.665,3.931 16.39,3.655 16.051,3.655Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val browseOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_browse_outline_16",
            defaultWidth = 14.dp,
            defaultHeight = 14.dp,
            viewportWidth = 14f,
            viewportHeight = 14f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M9.837,4.161V5.386H4.163V4.161H9.837Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.232,6.821V8.046H4.163V6.821H8.232Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.08,0.517C8.919,0.517 9.598,0.516 10.14,0.575C10.693,0.635 11.176,0.763 11.597,1.069C11.842,1.247 12.058,1.462 12.236,1.707C12.542,2.128 12.669,2.611 12.729,3.164C12.788,3.706 12.787,4.385 12.787,5.224V8.777C12.787,9.616 12.788,10.295 12.729,10.837C12.669,11.39 12.542,11.873 12.236,12.294C12.058,12.539 11.842,12.755 11.597,12.933C11.176,13.239 10.693,13.366 10.14,13.426C9.598,13.485 8.919,13.484 8.08,13.484H5.92C5.081,13.484 4.401,13.485 3.86,13.426C3.306,13.366 2.823,13.239 2.403,12.933C2.158,12.755 1.942,12.539 1.764,12.294C1.459,11.873 1.33,11.39 1.271,10.837C1.212,10.295 1.212,9.616 1.212,8.777V5.224C1.212,4.385 1.212,3.706 1.271,3.164C1.33,2.611 1.459,2.128 1.764,1.707C1.942,1.462 2.158,1.247 2.403,1.069C2.823,0.763 3.306,0.635 3.86,0.575C4.401,0.516 5.081,0.517 5.92,0.517H8.08ZM5.92,1.742C5.053,1.742 4.453,1.742 3.992,1.792C3.543,1.841 3.3,1.93 3.123,2.059C2.982,2.161 2.857,2.286 2.755,2.427C2.626,2.604 2.537,2.848 2.488,3.296C2.438,3.757 2.438,4.358 2.438,5.224V8.777C2.438,9.643 2.438,10.244 2.488,10.705C2.537,11.153 2.626,11.397 2.755,11.574C2.857,11.715 2.982,11.84 3.123,11.942C3.3,12.071 3.543,12.16 3.992,12.209C4.453,12.259 5.053,12.259 5.92,12.259H8.08C8.946,12.259 9.547,12.259 10.008,12.209C10.456,12.16 10.7,12.071 10.877,11.942C11.018,11.84 11.142,11.715 11.245,11.574C11.374,11.397 11.463,11.153 11.512,10.705C11.561,10.244 11.562,9.643 11.562,8.777V5.224C11.562,4.358 11.561,3.757 11.512,3.296C11.463,2.847 11.374,2.604 11.245,2.427C11.142,2.286 11.018,2.161 10.877,2.059C10.7,1.93 10.457,1.841 10.008,1.792C9.547,1.742 8.946,1.742 8.08,1.742H5.92Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val browseWarningOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_browse_warning_outline_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M1.719,4.174C1.719,2.515 3.064,1.17 4.723,1.17V2.483C3.789,2.483 3.032,3.24 3.032,4.174V4.956H1.719V4.174Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M1.719,11.798C1.719,13.457 3.064,14.802 4.723,14.802V13.489C3.789,13.489 3.032,12.732 3.032,11.798V11.016H1.719V11.798Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M14.282,4.174C14.282,2.515 12.937,1.17 11.278,1.17V2.483C12.212,2.483 12.969,3.24 12.969,4.174V4.956H14.282V4.174Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M14.282,11.798C14.282,13.457 12.937,14.802 11.278,14.802V13.489C12.212,13.489 12.969,12.732 12.969,11.798V11.016H14.282V11.798Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M9.67,1.17V2.483H6.33V1.17H9.67Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M9.67,13.489V14.802H6.33V13.489H9.67Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M12.969,6.552H14.282V9.323H12.969V6.552Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M1.719,6.552H3.032L3.032,9.323H1.719L1.719,6.552Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.04,5.192V6.505H4.961V5.192H11.04Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M9.32,8.042V9.355H4.961V8.042H9.32Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val cameraOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_camera_outline_20",
            defaultWidth = 22.dp,
            defaultHeight = 22.dp,
            viewportWidth = 22f,
            viewportHeight = 22f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M18.865,9.218C18.865,8.67 18.863,8.487 18.844,8.347C18.702,7.29 17.87,6.458 16.813,6.317C16.453,6.269 16.087,6.315 15.727,6.291C15.274,6.261 14.844,6.079 14.506,5.777C14.22,5.521 13.964,5.227 13.696,4.953C13.397,4.646 13.094,4.244 12.669,4.104C12.509,4.051 12.322,4.04 11.531,4.04H10.469C9.678,4.04 9.491,4.051 9.331,4.104C8.906,4.244 8.603,4.646 8.304,4.953C8.036,5.227 7.78,5.521 7.494,5.777C7.156,6.079 6.726,6.261 6.273,6.291C5.912,6.315 5.547,6.269 5.187,6.317C4.13,6.458 3.297,7.29 3.156,8.347C3.137,8.487 3.135,8.67 3.135,9.218V12.955C3.135,14.213 3.137,15.081 3.209,15.746C3.279,16.394 3.407,16.737 3.587,16.985C3.733,17.186 3.909,17.362 4.11,17.508C4.358,17.688 4.701,17.816 5.349,17.886C6.014,17.958 6.882,17.959 8.14,17.959H13.86C15.118,17.959 15.986,17.958 16.652,17.886C17.299,17.816 17.642,17.688 17.89,17.508C18.091,17.362 18.267,17.186 18.413,16.985C18.593,16.737 18.721,16.394 18.791,15.746C18.863,15.081 18.865,14.213 18.865,12.955V9.218ZM20.735,12.955C20.735,14.171 20.736,15.159 20.651,15.948C20.563,16.755 20.376,17.464 19.926,18.084C19.664,18.443 19.348,18.76 18.989,19.021C18.369,19.471 17.66,19.658 16.853,19.746C16.064,19.831 15.076,19.83 13.86,19.83H8.14C6.924,19.83 5.936,19.831 5.147,19.746C4.34,19.658 3.631,19.471 3.011,19.021C2.652,18.76 2.335,18.443 2.074,18.084C1.624,17.464 1.437,16.755 1.349,15.948C1.264,15.159 1.265,14.171 1.265,12.955V9.218C1.265,8.73 1.263,8.395 1.302,8.1C1.555,6.206 3.045,4.716 4.939,4.463C5.234,4.423 5.57,4.425 6.058,4.425C6.17,4.425 6.228,4.403 6.309,4.32L6.965,3.647C7.433,3.168 7.804,2.771 8.283,2.523C8.431,2.446 8.586,2.381 8.744,2.328C9.128,2.201 9.53,2.175 9.989,2.17H12.011C12.47,2.175 12.872,2.201 13.256,2.328C13.414,2.381 13.569,2.446 13.717,2.523C14.196,2.771 14.567,3.168 15.035,3.647L15.691,4.32C15.768,4.398 15.828,4.425 15.943,4.425C16.43,4.425 16.766,4.423 17.061,4.463C18.955,4.716 20.445,6.206 20.698,8.1C20.737,8.395 20.735,8.73 20.735,9.218V12.955Z").toNodes(),
                fill = SolidColor(Color(0xFFF8F8F8)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M13.365,11.8C13.365,10.494 12.306,9.435 11,9.435C9.694,9.435 8.635,10.494 8.635,11.8C8.635,13.106 9.694,14.165 11,14.165C12.306,14.165 13.365,13.106 13.365,11.8ZM15.235,11.8C15.235,14.139 13.339,16.035 11,16.035C8.661,16.035 6.765,14.139 6.765,11.8C6.765,9.461 8.661,7.565 11,7.565C13.339,7.565 15.235,9.461 15.235,11.8Z").toNodes(),
                fill = SolidColor(Color(0xFFF8F8F8)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val cancelSearchOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_cancel_search_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M9.322,1.688C13.644,1.688 17.195,4.99 17.593,9.209C17.101,8.875 16.55,8.623 15.958,8.472C15.435,6.191 13.768,4.347 11.592,3.577C11.708,3.821 11.815,4.081 11.911,4.353C12.326,5.53 12.593,7.041 12.677,8.702C11.671,9.117 10.824,9.838 10.25,10.748H7.457C7.528,12.349 7.782,13.761 8.149,14.801C8.368,15.421 8.613,15.87 8.849,16.148C9.085,16.426 9.247,16.464 9.322,16.464C9.396,16.464 9.559,16.426 9.795,16.148C9.863,16.069 9.93,15.975 9.998,15.868C10.418,16.706 11.057,17.414 11.839,17.919C11.045,18.171 10.199,18.309 9.322,18.309C4.733,18.308 1.011,14.587 1.011,9.998C1.012,5.409 4.733,1.688 9.322,1.688ZM2.554,10.748C2.852,13.461 4.742,15.695 7.271,16.493C7.066,16.138 6.887,15.734 6.734,15.301C6.301,14.076 6.027,12.489 5.955,10.748H2.554ZM7.051,3.577C4.633,4.432 2.843,6.614 2.554,9.248H5.944C5.992,7.373 6.273,5.657 6.734,4.353C6.83,4.081 6.935,3.821 7.051,3.577ZM9.322,3.188C9.247,3.189 9.085,3.227 8.849,3.505C8.613,3.783 8.368,4.232 8.149,4.853C7.755,5.966 7.492,7.505 7.444,9.248H11.201C11.152,7.505 10.889,5.966 10.496,4.853C10.276,4.232 10.032,3.783 9.795,3.505C9.559,3.227 9.396,3.189 9.322,3.188Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.534,13.653L16.535,14.404L13.135,14.403V12.903H16.535L16.534,13.653Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M14.835,9.5C17.129,9.5 18.989,11.36 18.989,13.653C18.989,15.947 17.129,17.806 14.835,17.807C12.542,17.807 10.682,15.947 10.682,13.653C10.682,11.36 12.542,9.5 14.835,9.5ZM14.835,11C13.37,11 12.182,12.188 12.182,13.653C12.182,15.119 13.37,16.307 14.835,16.307C16.301,16.306 17.489,15.118 17.489,13.653C17.489,12.188 16.3,11 14.835,11Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val ccOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_cc_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M2.42,10C2.42,9.11 2.407,8.417 2.607,7.829C2.957,6.801 3.765,5.992 4.793,5.643C5.381,5.443 6.074,5.456 6.964,5.456H8.641V7.456H6.964C5.911,7.456 5.632,7.47 5.437,7.536C4.996,7.686 4.65,8.032 4.5,8.473C4.434,8.668 4.42,8.947 4.42,10C4.42,11.053 4.434,11.332 4.5,11.527C4.65,11.968 4.996,12.314 5.437,12.464C5.632,12.53 5.911,12.544 6.964,12.544H8.641V14.544H6.964C6.074,14.544 5.381,14.557 4.793,14.357C3.765,14.007 2.956,13.199 2.607,12.171C2.407,11.583 2.42,10.89 2.42,10Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M10.359,10C10.359,9.11 10.346,8.417 10.545,7.829C10.896,6.801 11.703,5.992 12.732,5.643C13.32,5.443 14.013,5.456 14.903,5.456H16.58V7.456H14.903C13.85,7.456 13.571,7.47 13.376,7.536C12.935,7.686 12.589,8.032 12.439,8.473C12.373,8.668 12.359,8.947 12.359,10C12.359,11.053 12.373,11.332 12.439,11.527C12.589,11.968 12.935,12.314 13.376,12.464C13.571,12.53 13.85,12.544 14.903,12.544H16.58V14.544H14.903C14.013,14.544 13.32,14.557 12.732,14.357C11.703,14.007 10.896,13.199 10.545,12.171C10.346,11.583 10.359,10.89 10.359,10Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val checkOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_check_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M17.086,5.57L10.532,14.026C10.296,14.331 10.08,14.613 9.876,14.822C9.663,15.042 9.393,15.259 9.024,15.354C8.831,15.403 8.632,15.42 8.434,15.404C8.054,15.374 7.752,15.206 7.504,15.026C7.268,14.853 7.006,14.612 6.722,14.353L2.914,10.87L3.994,9.69L7.802,13.172C8.114,13.457 8.3,13.625 8.447,13.733C8.58,13.829 8.62,13.819 8.728,13.708C8.855,13.578 9.009,13.38 9.267,13.047L15.821,4.59L17.086,5.57Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val checkedFilled16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_checked_filled_16",
            defaultWidth = 15.dp,
            defaultHeight = 15.dp,
            viewportWidth = 15f,
            viewportHeight = 15f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13.488,4.212L8.305,10.101C7.96,10.493 7.647,10.851 7.36,11.111C7.06,11.384 6.692,11.642 6.205,11.728C5.951,11.773 5.69,11.776 5.435,11.734C4.947,11.655 4.575,11.401 4.271,11.133C3.98,10.877 3.663,10.524 3.312,10.137L1.521,8.161L2.979,6.838L4.771,8.816C5.153,9.237 5.384,9.491 5.572,9.657C5.674,9.747 5.73,9.78 5.752,9.79C5.788,9.796 5.824,9.796 5.86,9.79C5.827,9.796 5.865,9.809 6.037,9.653C6.223,9.484 6.451,9.227 6.827,8.8L12.012,2.912L13.488,4.212Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val checklistOutline10: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_checklist_outline_10",
            defaultWidth = 10.dp,
            defaultHeight = 10.dp,
            viewportWidth = 10f,
            viewportHeight = 10f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M1.32,2.23 L2.56,2.23").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF7D7F85)),
                strokeLineWidth = 1.15f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M3.42,2.23 L8.68,2.23").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF7D7F85)),
                strokeLineWidth = 0.85f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M1.32,5 L2.56,5").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF7D7F85)),
                strokeLineWidth = 1.15f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M3.42,5 L8.68,5").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF7D7F85)),
                strokeLineWidth = 0.85f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M1.32,7.77 L2.56,7.77").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF7D7F85)),
                strokeLineWidth = 1.15f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M3.42,7.77 L8.68,7.77").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF7D7F85)),
                strokeLineWidth = 0.85f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val checklistOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_checklist_outline_16",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M16.047,12.062V13.502H9.244V12.062H16.047Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.047,4.498V5.938H9.244V4.498H16.047Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M6.279,12.782C6.279,11.985 5.633,11.339 4.836,11.339C4.039,11.339 3.393,11.985 3.393,12.782C3.393,13.579 4.039,14.225 4.836,14.225C5.633,14.225 6.279,13.579 6.279,12.782ZM7.72,12.782C7.72,14.374 6.428,15.666 4.836,15.666C3.243,15.666 1.953,14.374 1.953,12.782C1.953,11.19 3.244,9.899 4.836,9.899C6.428,9.899 7.72,11.19 7.72,12.782Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M6.279,5.218C6.279,4.421 5.633,3.775 4.836,3.775C4.039,3.775 3.393,4.421 3.393,5.218C3.393,6.015 4.039,6.661 4.836,6.661C5.633,6.661 6.279,6.015 6.279,5.218ZM7.719,5.218C7.719,6.81 6.428,8.101 4.836,8.101C3.244,8.101 1.953,6.81 1.953,5.218C1.953,3.625 3.244,2.335 4.836,2.335C6.428,2.335 7.719,3.625 7.719,5.218Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val checklistOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_checklist_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M2.621,16.083V18.003H11.691V16.083H2.621Z").toNodes(),
                fill = SolidColor(Color(0xFF545557)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M21.379,5.997V7.917H12.309V5.997H21.379Z").toNodes(),
                fill = SolidColor(Color(0xFF545557)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M15.645,17.043C15.645,16.047 16.402,15.227 17.372,15.128L17.569,15.118C18.631,15.118 19.493,15.98 19.493,17.043L19.484,17.239C19.392,18.145 18.671,18.865 17.766,18.957L17.569,18.967C16.572,18.967 15.753,18.21 15.655,17.239L15.645,17.043ZM13.725,17.043C13.725,19.166 15.446,20.887 17.569,20.887C19.692,20.887 21.413,19.166 21.413,17.043C21.413,14.92 19.692,13.198 17.569,13.198C15.446,13.199 13.725,14.92 13.725,17.043Z").toNodes(),
                fill = SolidColor(Color(0xFF545557)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.355,6.957C8.355,5.894 7.493,5.033 6.431,5.033C5.368,5.033 4.507,5.894 4.507,6.957C4.507,7.953 5.264,8.773 6.234,8.872L6.431,8.881C7.427,8.881 8.247,8.124 8.346,7.154L8.355,6.957ZM10.275,6.957C10.275,9.08 8.554,10.801 6.431,10.801C4.308,10.801 2.587,9.08 2.587,6.957C2.587,4.834 4.308,3.113 6.431,3.113C8.554,3.113 10.275,4.834 10.275,6.957Z").toNodes(),
                fill = SolidColor(Color(0xFF545557)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val chevronDownOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_chevron_down_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M4.375,8.19L8.728,12.539C9.258,13.068 9.523,13.333 9.844,13.384C9.947,13.401 10.053,13.401 10.156,13.384C10.477,13.333 10.742,13.068 11.272,12.539L15.625,8.19").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val chevronExpandVerticalOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_chevron_expand_vertical_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M6,12.7L8.955,14.948C9.4,15.286 9.622,15.455 9.875,15.488C9.958,15.499 10.041,15.499 10.124,15.488C10.377,15.455 10.599,15.286 11.044,14.948L14,12.7").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF8E9094)),
                strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M14,7.742L11.044,5.494C10.599,5.156 10.377,4.987 10.124,4.954C10.041,4.943 9.958,4.943 9.875,4.954C9.622,4.987 9.4,5.156 8.955,5.494L6,7.742").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF8E9094)),
                strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val chevronLeftOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_chevron_left_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M11.62,4.375L7.271,8.728C6.742,9.258 6.477,9.523 6.426,9.844C6.41,9.947 6.41,10.053 6.426,10.156C6.477,10.477 6.742,10.742 7.271,11.272L11.62,15.625").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val chevronLeftOutline20Small: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_chevron_left_outline_20_small",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M6.751,4.764L2.787,8.727C2.257,9.257 1.992,9.523 1.941,9.843C1.925,9.947 1.925,10.053 1.941,10.156C1.992,10.477 2.257,10.742 2.787,11.273L6.751,15.236").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF8E9094)),
                strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val chevronLeftSmallOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_chevron_left_small_outline_16",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M8.021,8.728L10.805,5.941C11.266,5.481 11.496,5.25 11.697,5.282C11.759,5.292 11.819,5.316 11.87,5.354C12.035,5.473 12.035,5.799 12.035,6.45L12.035,13.551C12.035,14.202 12.035,14.528 11.87,14.648C11.819,14.685 11.759,14.709 11.697,14.719C11.496,14.751 11.266,14.521 10.805,14.06L8.021,11.273C7.491,10.743 7.226,10.478 7.176,10.157C7.159,10.053 7.159,9.948 7.176,9.844C7.226,9.524 7.491,9.258 8.021,8.728Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val chevronRightFill: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_chevron_right_fill",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M15.667,8.002C15.667,8.124 15.643,8.24 15.594,8.35C15.549,8.456 15.476,8.558 15.374,8.656L10.673,13.259C10.514,13.413 10.321,13.491 10.092,13.491C9.946,13.491 9.809,13.454 9.683,13.381C9.556,13.307 9.455,13.21 9.377,13.087C9.304,12.965 9.267,12.827 9.267,12.672C9.267,12.448 9.353,12.248 9.524,12.073L13.711,8.002L9.524,3.931C9.353,3.76 9.267,3.56 9.267,3.332C9.267,3.181 9.304,3.044 9.377,2.922C9.455,2.796 9.556,2.696 9.683,2.623C9.809,2.549 9.946,2.513 10.092,2.513C10.321,2.513 10.514,2.59 10.673,2.745L15.374,7.348C15.472,7.445 15.545,7.547 15.594,7.653C15.643,7.759 15.667,7.875 15.667,8.002Z").toNodes(),
                fill = SolidColor(Color(0xFFBBBBBB)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val chevronRightOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_chevron_right_outline_20",
            defaultWidth = 21.dp,
            defaultHeight = 20.dp,
            viewportWidth = 21f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M7.587,15.625L11.936,11.272C12.465,10.742 12.73,10.477 12.781,10.156C12.797,10.053 12.797,9.947 12.781,9.844C12.73,9.523 12.465,9.258 11.936,8.728L7.587,4.375").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF8E9094)),
                strokeLineWidth = 1.5f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val chevronRightOutline20Small: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_chevron_right_outline_20_small",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13.252,4.764L17.215,8.727C17.745,9.257 18.011,9.523 18.061,9.843C18.078,9.947 18.078,10.053 18.061,10.156C18.011,10.477 17.745,10.742 17.215,11.273L13.252,15.236").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF8E9094)),
                strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val chevronUpFill: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_chevron_up_fill",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 17f,
            viewportHeight = 17f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M8.502,5.389C8.624,5.389 8.74,5.414 8.85,5.462C8.956,5.507 9.058,5.581 9.156,5.682L13.759,10.383C13.914,10.542 13.991,10.736 13.991,10.964C13.991,11.111 13.954,11.247 13.881,11.373C13.808,11.5 13.71,11.602 13.588,11.679C13.465,11.752 13.327,11.789 13.172,11.789C12.948,11.789 12.748,11.703 12.573,11.532L8.502,7.345L4.431,11.532C4.26,11.703 4.06,11.789 3.832,11.789C3.681,11.789 3.545,11.752 3.422,11.679C3.296,11.602 3.196,11.5 3.123,11.373C3.049,11.247 3.013,11.111 3.013,10.964C3.013,10.736 3.09,10.542 3.245,10.383L7.848,5.682C7.946,5.585 8.048,5.511 8.153,5.462C8.259,5.414 8.376,5.389 8.502,5.389Z").toNodes(),
                fill = SolidColor(Color(0xFFA2A4A6)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val closeMidOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_close_mid_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M16.944,4.258L11.201,10L16.944,15.743L15.743,16.944L10,11.201L4.257,16.944L3.056,15.743L8.799,10L3.056,4.257L4.258,3.056L10,8.798L15.743,3.056L16.944,4.258Z").toNodes(),
                fill = SolidColor(Color(0xFFB9BABB)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val closeOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_close_outline_16",
            defaultWidth = 14.dp,
            defaultHeight = 14.dp,
            viewportWidth = 14f,
            viewportHeight = 14f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10.964,3.964L7.928,7L10.964,10.036L10.036,10.964L7,7.928L3.964,10.964L3.036,10.036L6.072,7L3.036,3.964L3.964,3.036L7,6.072L10.036,3.036L10.964,3.964Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val closeOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_close_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M15.565,5.565L11.131,10L15.565,14.435L14.434,15.565L10,11.131L5.565,15.565L4.435,14.435L8.869,10L4.435,5.565L5.565,4.435L10,8.869L14.434,4.435L15.565,5.565Z").toNodes(),
                fill = SolidColor(Color(0xFF545557)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val closeOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_close_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M5.1,5.1L18.9,18.9M5.1,18.9L18.899,5.1").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFFF8F8F8)),
                strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val closeRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_close_regular",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M3.139,3.138C3.407,2.869 3.843,2.869 4.111,3.138L8.044,7.071L11.978,3.138C12.246,2.869 12.682,2.869 12.95,3.138C13.219,3.406 13.219,3.842 12.95,4.11L9.017,8.043L12.95,11.977C13.219,12.245 13.219,12.681 12.95,12.949C12.682,13.217 12.246,13.217 11.978,12.949L8.044,9.016L4.111,12.949C3.843,13.217 3.407,13.217 3.139,12.949C2.87,12.681 2.87,12.245 3.139,11.977L7.072,8.043L3.139,4.11C2.87,3.842 2.87,3.406 3.139,3.138Z").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 0.28125f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val copyOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_copy_outline",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M8.153,5.241C9.196,5.241 10.031,5.241 10.702,5.3C11.383,5.36 11.972,5.486 12.508,5.784C13.207,6.173 13.784,6.75 14.173,7.449C14.472,7.985 14.598,8.574 14.658,9.256C14.717,9.927 14.716,10.762 14.716,11.805C14.716,12.848 14.717,13.682 14.658,14.354C14.598,15.035 14.472,15.624 14.173,16.16C13.784,16.859 13.207,17.436 12.508,17.825C11.972,18.124 11.383,18.25 10.702,18.31C10.031,18.369 9.196,18.368 8.153,18.368C7.11,18.368 6.275,18.369 5.604,18.31C4.923,18.25 4.333,18.124 3.797,17.825C3.098,17.436 2.522,16.859 2.132,16.16C1.834,15.624 1.708,15.035 1.648,14.354C1.589,13.682 1.589,12.848 1.589,11.805C1.589,10.762 1.589,9.927 1.648,9.256C1.708,8.575 1.834,7.985 2.132,7.449C2.522,6.75 3.098,6.173 3.797,5.784C4.333,5.486 4.923,5.36 5.604,5.3C6.275,5.241 7.11,5.241 8.153,5.241ZM8.153,6.841C7.081,6.841 6.329,6.842 5.745,6.894C5.17,6.944 4.834,7.038 4.576,7.182C4.137,7.426 3.774,7.788 3.53,8.228C3.386,8.485 3.292,8.822 3.242,9.396C3.19,9.981 3.189,10.733 3.189,11.805C3.189,12.876 3.19,13.628 3.242,14.213C3.292,14.787 3.386,15.124 3.53,15.382C3.774,15.821 4.137,16.183 4.576,16.428C4.834,16.571 5.17,16.665 5.745,16.716C6.329,16.767 7.081,16.769 8.153,16.769C9.224,16.769 9.976,16.767 10.561,16.716C11.135,16.665 11.472,16.571 11.73,16.428C12.169,16.183 12.531,15.821 12.776,15.382C12.919,15.124 13.014,14.787 13.064,14.213C13.115,13.628 13.117,12.876 13.117,11.805C13.117,10.733 13.115,9.981 13.064,9.396C13.013,8.822 12.919,8.485 12.776,8.228C12.531,7.788 12.169,7.426 11.73,7.182C11.472,7.038 11.135,6.944 10.561,6.894C9.976,6.842 9.224,6.841 8.153,6.841Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.897,1.632C12.941,1.632 13.773,1.632 14.441,1.69C15.119,1.75 15.701,1.875 16.228,2.168C16.919,2.553 17.489,3.124 17.874,3.814C18.167,4.342 18.292,4.924 18.352,5.602C18.411,6.27 18.411,7.102 18.411,8.146C18.411,9.189 18.411,10.021 18.352,10.689C18.292,11.368 18.167,11.949 17.874,12.477C17.489,13.167 16.919,13.737 16.228,14.122C16.02,14.238 15.802,14.327 15.575,14.396V12.766C15.987,12.518 16.329,12.168 16.563,11.747C16.711,11.481 16.807,11.135 16.858,10.558C16.91,9.97 16.911,9.216 16.911,8.146C16.911,7.075 16.91,6.32 16.858,5.732C16.807,5.155 16.711,4.81 16.563,4.544C16.314,4.097 15.945,3.728 15.498,3.479C15.232,3.331 14.886,3.235 14.309,3.185C13.722,3.133 12.967,3.132 11.897,3.132C10.826,3.132 10.071,3.133 9.484,3.185C8.907,3.235 8.562,3.331 8.295,3.479C7.906,3.697 7.576,4.005 7.333,4.377H5.675C5.74,4.182 5.819,3.995 5.919,3.814C6.304,3.123 6.875,2.553 7.566,2.168C8.093,1.875 8.675,1.75 9.353,1.69C10.021,1.632 10.853,1.632 11.897,1.632Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val cropOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_crop_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M5.784,2.316V5.783H2.315V7.584H5.784V16.316C5.784,17.366 6.635,18.217 7.685,18.217H16.415V21.684H18.216V18.217H21.685V16.416H18.216V7.684C18.216,6.634 17.365,5.783 16.315,5.783H7.585V2.316H5.784ZM16.315,7.584C16.371,7.584 16.415,7.628 16.415,7.684V16.416H7.685C7.629,16.416 7.585,16.372 7.585,16.316V7.584H16.315Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val dataRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_data_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M17.219,7.511C17.219,7.509 17.219,7.491 17.201,7.45C17.181,7.403 17.136,7.322 17.04,7.213C16.841,6.988 16.485,6.715 15.934,6.452C14.835,5.926 13.213,5.561 11.344,5.561C9.474,5.561 7.853,5.926 6.754,6.452C6.203,6.715 5.846,6.988 5.647,7.213C5.552,7.322 5.506,7.403 5.486,7.45C5.469,7.491 5.469,7.509 5.469,7.511V7.512C5.469,7.517 5.47,7.535 5.486,7.573C5.506,7.62 5.552,7.701 5.647,7.809C5.846,8.034 6.203,8.307 6.754,8.571C7.853,9.097 9.474,9.462 11.344,9.462C13.213,9.462 14.835,9.097 15.934,8.571C16.485,8.307 16.841,8.034 17.04,7.809C17.136,7.701 17.181,7.62 17.201,7.573C17.218,7.535 17.219,7.517 17.219,7.512V7.511ZM17.219,9.618C15.879,10.471 13.746,11.023 11.344,11.023C8.942,11.023 6.809,10.471 5.469,9.618L5.469,11.803C5.469,11.803 5.476,11.94 5.742,12.192C5.994,12.431 6.398,12.691 6.949,12.931C8.046,13.409 9.602,13.754 11.344,13.754C11.356,13.754 11.367,13.754 11.379,13.753C11.148,14.234 10.991,14.757 10.923,15.308C9.179,15.258 7.591,14.898 6.392,14.375C6.06,14.231 5.749,14.07 5.469,13.894L5.469,15.704C5.469,15.881 5.542,16.109 5.786,16.381C6.033,16.658 6.427,16.942 6.969,17.198C8.051,17.709 9.597,18.045 11.344,18.045C11.345,18.045 11.345,18.045 11.346,18.045C11.61,18.618 11.979,19.133 12.427,19.566C12.073,19.592 11.711,19.606 11.344,19.606C9.441,19.606 7.682,19.243 6.372,18.624C5.718,18.315 5.143,17.927 4.722,17.457C4.297,16.983 4,16.389 4,15.704V11.803V7.511C4,5.572 7.288,4 11.344,4C15.4,4 18.688,5.572 18.688,7.511V11.701C18.242,11.411 17.747,11.189 17.219,11.05V9.618Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M15.992,12.262C15.599,12.262 15.236,12.469 15.038,12.808L14.739,13.317C14.674,13.429 14.554,13.498 14.424,13.499L13.836,13.503C13.444,13.507 13.083,13.717 12.887,14.057L12.725,14.337C12.53,14.676 12.528,15.093 12.721,15.433L13.013,15.95C13.076,16.062 13.076,16.2 13.013,16.312L12.721,16.829C12.528,17.169 12.53,17.586 12.725,17.924L12.887,18.205C13.083,18.545 13.444,18.755 13.836,18.758L14.424,18.763C14.554,18.764 14.674,18.833 14.739,18.945L15.038,19.454C15.236,19.792 15.599,20 15.992,20H16.312C16.704,20 17.067,19.792 17.266,19.454L17.565,18.945C17.63,18.833 17.75,18.764 17.88,18.763L18.468,18.758C18.86,18.755 19.221,18.545 19.417,18.205L19.579,17.924C19.774,17.586 19.775,17.169 19.583,16.829L19.291,16.312C19.228,16.2 19.228,16.062 19.291,15.95L19.583,15.433C19.775,15.093 19.774,14.676 19.579,14.337L19.417,14.057C19.221,13.717 18.86,13.507 18.468,13.503L17.88,13.499C17.75,13.498 17.63,13.429 17.565,13.317L17.266,12.808C17.067,12.469 16.704,12.262 16.312,12.262H15.992ZM16.152,17.737C17.039,17.737 17.758,17.018 17.758,16.131C17.758,15.244 17.039,14.524 16.152,14.524C15.265,14.524 14.545,15.244 14.545,16.131C14.545,17.018 15.265,17.737 16.152,17.737Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val dislikeFill: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_dislike_fill",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M3.54,4.867L2.433,7.784C2.208,8.377 2.095,8.674 2.05,8.909C1.753,10.451 2.806,11.932 4.388,12.197C4.628,12.237 4.952,12.237 5.599,12.237H6.684C7.162,12.237 7.401,12.237 7.544,12.363C7.59,12.404 7.627,12.452 7.655,12.506C7.742,12.674 7.678,12.899 7.551,13.35L7.006,15.283L6.994,15.325C6.766,16.163 7.009,17.058 7.634,17.674L7.665,17.705L7.687,17.726C8.109,18.124 8.789,18.083 9.158,17.637L9.177,17.614L13.507,12.231C13.7,11.991 13.853,11.719 13.853,11.414V5.053C13.853,3.367 12.456,2 10.732,2L7.754,2C6.415,2 5.745,2 5.198,2.283C5.019,2.376 4.852,2.489 4.701,2.621C4.239,3.025 4.006,3.639 3.54,4.867Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M15.422,4.565C15.422,3.022 14.174,2.122 14.174,2.122H15.697C16.969,2.122 18,3.131 18,4.375V9.641C18,10.885 16.969,11.893 15.697,11.893H15.422V4.565Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val dislikeOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_dislike_outline",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M9.702,17.874C9.123,18.589 8.085,18.695 7.376,18.144L7.239,18.025L7.233,18.019L7.213,18L7.211,17.997L7.182,17.968L7.179,17.964C6.413,17.194 6.114,16.074 6.394,15.025L6.396,15.02L6.407,14.982V14.98L6.911,13.156C6.921,13.12 6.93,13.087 6.938,13.056C6.906,13.056 6.871,13.055 6.833,13.055H5.83C5.268,13.055 4.886,13.058 4.584,13.006C2.71,12.686 1.461,10.893 1.812,9.025C1.869,8.724 2.005,8.368 2.2,7.841L3.224,5.087C3.434,4.521 3.609,4.049 3.788,3.674C3.973,3.288 4.185,2.951 4.5,2.67C4.685,2.505 4.889,2.363 5.108,2.248C5.482,2.051 5.872,1.969 6.299,1.93C6.713,1.893 7.216,1.893 7.821,1.893H13.343C14.191,1.893 14.888,1.892 15.446,1.953C16.018,2.015 16.534,2.149 16.986,2.477C17.246,2.666 17.474,2.894 17.663,3.153C17.992,3.605 18.125,4.12 18.187,4.692C18.248,5.25 18.247,5.946 18.247,6.794V8.577C18.247,9.288 18.254,9.802 18.128,10.241C17.837,11.253 17.045,12.044 16.033,12.335C15.594,12.46 15.08,12.453 14.368,12.453C14.149,12.453 14.122,12.455 14.103,12.458C14.025,12.47 13.953,12.506 13.894,12.559C13.881,12.572 13.863,12.591 13.727,12.763L9.726,17.843L9.725,17.844L9.707,17.867L9.702,17.874ZM12.708,5.525C12.708,4.349 11.753,3.393 10.573,3.393H7.821C7.189,3.393 6.764,3.395 6.433,3.424C6.115,3.453 5.941,3.504 5.809,3.574C5.697,3.633 5.593,3.706 5.499,3.79C5.387,3.889 5.279,4.033 5.142,4.321C4.998,4.621 4.85,5.019 4.63,5.611L3.606,8.364C3.386,8.957 3.314,9.16 3.287,9.302C3.091,10.345 3.788,11.349 4.837,11.528C4.98,11.552 5.197,11.555 5.83,11.555H6.833C7.035,11.555 7.253,11.554 7.434,11.575C7.625,11.597 7.896,11.656 8.131,11.868C8.241,11.968 8.332,12.087 8.399,12.22C8.542,12.503 8.526,12.78 8.497,12.97C8.469,13.15 8.41,13.361 8.356,13.555L7.853,15.38L7.843,15.417C7.702,15.949 7.854,16.516 8.243,16.908V16.909L8.266,16.93L8.28,16.945L8.309,16.968C8.382,17.013 8.48,16.998 8.536,16.929L8.549,16.916L8.548,16.915L12.549,11.835C12.601,11.768 12.654,11.701 12.708,11.637V5.525ZM16.747,6.794C16.747,5.912 16.746,5.311 16.696,4.853C16.648,4.409 16.562,4.189 16.45,4.035C16.354,3.903 16.238,3.787 16.105,3.691C15.951,3.579 15.73,3.492 15.285,3.444C14.857,3.398 14.305,3.395 13.517,3.395C13.951,3.993 14.208,4.729 14.208,5.525V10.955C14.261,10.954 14.315,10.953 14.368,10.953C15.183,10.953 15.434,10.945 15.619,10.892C16.135,10.744 16.538,10.341 16.687,9.827C16.74,9.642 16.747,9.392 16.747,8.577V6.794Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val downloadOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_download_outline",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M7.36,10.266L7.36,1.2L8.64,1.2L8.64,10.266L7.36,10.266Z").toNodes(),
                fill = SolidColor(Color(0xFF7F8287)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.278,10.982C8.094,11.011 7.907,11.011 7.723,10.982C7.386,10.928 7.117,10.764 6.878,10.572C6.649,10.388 6.395,10.133 6.105,9.843L2.448,6.186L3.353,5.28L7.01,8.939C7.32,9.249 7.518,9.445 7.68,9.575C7.833,9.698 7.897,9.714 7.923,9.718C7.974,9.726 8.026,9.726 8.077,9.718C8.104,9.714 8.167,9.698 8.32,9.575C8.482,9.445 8.679,9.249 8.99,8.939L12.648,5.28L13.1,5.734L13.552,6.186L9.895,9.843C9.604,10.133 9.351,10.388 9.122,10.572C8.884,10.764 8.615,10.928 8.278,10.982Z").toNodes(),
                fill = SolidColor(Color(0xFF7F8287)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M15.432,11.505L15.181,13.011C14.947,14.413 13.734,15.44 12.314,15.44H3.687C2.266,15.44 1.054,14.413 0.82,13.011L0.569,11.505L1.831,11.294L2.082,12.8C2.213,13.585 2.892,14.16 3.687,14.16H12.314C13.108,14.16 13.787,13.585 13.918,12.8L14.169,11.294L15.432,11.505Z").toNodes(),
                fill = SolidColor(Color(0xFF7F8287)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val downloadOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_download_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M18.039,13.357L17.714,15.31C17.489,16.66 16.32,17.65 14.951,17.65H5.049C3.68,17.65 2.511,16.66 2.286,15.31L1.961,13.357L3.539,13.093L3.865,15.047C3.962,15.626 4.462,16.05 5.049,16.05H14.951C15.538,16.05 16.038,15.626 16.135,15.047L16.461,13.093L18.039,13.357Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M10.8,10.69L14.872,6.619L16.003,7.75L11.839,11.915C11.587,12.167 11.352,12.402 11.138,12.575C10.912,12.757 10.636,12.929 10.281,12.985C10.095,13.014 9.905,13.014 9.719,12.985C9.364,12.929 9.088,12.757 8.862,12.575C8.648,12.402 8.414,12.167 8.161,11.915L3.997,7.75L5.128,6.619L9.2,10.69V2.35H10.8V10.69Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val dsNewChatOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_ds_new_chat_outline_20",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12,2.4C6.699,2.4 2.4,6.698 2.4,12C2.4,13.47 2.731,14.867 3.324,16.115L3.689,16.882L5.221,16.153L4.857,15.386L4.684,14.996C4.306,14.073 4.097,13.061 4.097,12C4.097,7.635 7.636,4.097 12,4.097C16.365,4.097 19.903,7.635 19.903,12C19.903,16.364 16.365,19.903 12,19.903C11.107,19.903 10.452,19.839 9.873,19.679C9.301,19.521 8.759,19.258 8.11,18.805C7.159,18.142 5.797,17.908 4.673,18.618L4.655,18.629L4.637,18.641L3.89,19.168L4.453,20.847L5.59,20.045C6,19.795 6.607,19.827 7.138,20.198C7.907,20.734 8.623,21.095 9.42,21.315C10.209,21.533 11.035,21.6 12,21.6C17.302,21.6 21.6,17.302 21.6,12C21.6,6.698 17.302,2.4 12,2.4ZM7.5,12.668V14.365H12.165V12.668H7.5ZM7.5,11.081H16.542V9.384H7.5V11.081Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val dsSearchOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_ds_search_outline_16",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13.217,7.541C13.217,4.389 10.661,1.833 7.509,1.833C4.357,1.833 1.802,4.389 1.802,7.541C1.802,10.693 4.357,13.248 7.509,13.248C10.661,13.248 13.216,10.693 13.217,7.541ZM14.679,7.541C14.679,11.5 11.469,14.71 7.509,14.71C3.549,14.71 0.339,11.5 0.338,7.541C0.338,3.581 3.549,0.37 7.509,0.37C11.469,0.37 14.679,3.581 14.679,7.541Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M17.662,16.595L16.627,17.63L12.822,13.825L13.857,12.791L17.662,16.595Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val dsUserOutline14: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_ds_user_outline_14",
            defaultWidth = 12.dp,
            defaultHeight = 12.dp,
            viewportWidth = 12f,
            viewportHeight = 12f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M7.988,4.172C7.988,3.074 7.098,2.184 6,2.184C4.902,2.184 4.012,3.074 4.012,4.172C4.012,5.27 4.902,6.16 6,6.16C7.098,6.16 7.988,5.27 7.988,4.172ZM8.887,4.172C8.887,5.767 7.595,7.059 6,7.059C4.405,7.059 3.113,5.767 3.113,4.172C3.113,2.577 4.405,1.285 6,1.285C7.595,1.285 8.887,2.577 8.887,4.172Z").toNodes(),
                fill = SolidColor(Color(0xFF81858C)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M6,7.8C7.203,7.8 8.222,7.94 9.018,8.299C9.835,8.668 10.405,9.265 10.693,10.123L9.839,10.408C9.636,9.804 9.251,9.392 8.647,9.119C8.021,8.837 7.152,8.7 6,8.7C4.848,8.7 3.979,8.837 3.353,9.119C2.749,9.392 2.364,9.804 2.161,10.408L1.307,10.123C1.595,9.265 2.165,8.668 2.982,8.299C3.778,7.94 4.797,7.8 6,7.8Z").toNodes(),
                fill = SolidColor(Color(0xFF81858C)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val editOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_edit_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12.038,2.964C12.859,2.484 13.875,2.484 14.696,2.964C15.009,3.148 15.292,3.439 15.648,3.795C16.004,4.151 16.296,4.434 16.48,4.748C16.96,5.568 16.959,6.584 16.48,7.404C16.296,7.719 16.004,8.001 15.648,8.358L8.66,15.346C8.265,15.741 7.97,16.043 7.604,16.257C7.237,16.47 6.829,16.577 6.289,16.726L5.046,17.069C4.556,17.204 4.099,17.332 3.733,17.377C3.36,17.423 2.841,17.415 2.435,17.009C2.028,16.603 2.02,16.084 2.066,15.711C2.111,15.344 2.239,14.888 2.374,14.398L2.717,13.154C2.866,12.615 2.972,12.207 3.186,11.84C3.399,11.473 3.702,11.179 4.098,10.783L11.34,3.541C11.584,3.3 11.803,3.102 12.038,2.964ZM17.953,17.243H9.368L10.969,15.643H17.953V17.243ZM5.229,11.914C4.776,12.367 4.654,12.499 4.569,12.645C4.483,12.791 4.43,12.963 4.26,13.58L3.917,14.822C3.797,15.259 3.718,15.552 3.678,15.765C3.89,15.724 4.183,15.647 4.62,15.526L5.863,15.184C6.48,15.014 6.652,14.96 6.798,14.875C6.944,14.79 7.076,14.667 7.529,14.214L12.807,8.935L10.507,6.635L5.229,11.914ZM13.887,4.345C13.566,4.157 13.168,4.157 12.847,4.345C12.755,4.399 12.644,4.5 12.217,4.927L11.639,5.504L13.939,7.804L14.517,7.226C14.943,6.799 15.045,6.689 15.099,6.597C15.287,6.276 15.287,5.877 15.099,5.556C15.045,5.464 14.944,5.354 14.517,4.927C14.09,4.5 13.979,4.399 13.887,4.345Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val ellipsis: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_ellipsis",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M5,10C5,10.92 4.254,11.667 3.333,11.667C2.413,11.667 1.667,10.92 1.667,10C1.667,9.08 2.413,8.333 3.333,8.333C4.254,8.333 5,9.08 5,10ZM11.667,10C11.667,10.921 10.92,11.667 10,11.667C9.08,11.667 8.333,10.921 8.333,10C8.333,9.08 9.08,8.333 10,8.333C10.92,8.333 11.667,9.08 11.667,10ZM16.667,11.667C17.587,11.667 18.333,10.921 18.333,10C18.333,9.08 17.587,8.333 16.667,8.333C15.746,8.333 15,9.08 15,10C15,10.921 15.746,11.667 16.667,11.667Z").toNodes(),
                fill = SolidColor(Color(0xFF8B8B8B)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val ellipsisOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_ellipsis_outline",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M5.341,10C5.341,10.858 4.646,11.554 3.788,11.554C2.929,11.554 2.234,10.858 2.234,10C2.234,9.142 2.929,8.446 3.788,8.446C4.646,8.446 5.341,9.142 5.341,10Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.551,10C11.551,10.858 10.855,11.554 9.997,11.554C9.139,11.554 8.443,10.858 8.443,10C8.443,9.142 9.139,8.446 9.997,8.446C10.855,8.446 11.551,9.142 11.551,10Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M17.767,10C17.767,10.858 17.071,11.554 16.213,11.554C15.355,11.554 14.659,10.858 14.659,10C14.659,9.142 15.355,8.446 16.213,8.446C17.071,8.446 17.767,9.142 17.767,10Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val emptyIconContainer: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_empty_icon_container",
            defaultWidth = 40.dp,
            defaultHeight = 40.dp,
            viewportWidth = 40f,
            viewportHeight = 40f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M32.816,19.999C32.816,27.158 27.012,32.961 19.854,32.961C12.695,32.961 6.892,27.158 6.892,19.999C6.892,12.84 12.695,7.037 19.854,7.037C24.743,7.037 29,9.744 31.208,13.741").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF7D7F85)),
                strokeLineWidth = 2.34f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M33.569,16.48H26.835C26.354,16.48 26.113,15.898 26.453,15.558L33.187,8.824C33.527,8.484 34.109,8.725 34.109,9.206V15.94C34.109,16.238 33.867,16.48 33.569,16.48Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val enhanceOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_enhance_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M17.256,3.875V5.375H2.744V3.875H17.256Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M17.256,7.459V8.959H2.744V7.459H17.256Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M17.256,11.043V12.543H2.744V11.043H17.256Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M10.967,14.627V16.127H2.744V14.627H10.967Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val excerptQuote: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_excerpt_quote",
            defaultWidth = 89.1014.dp,
            defaultHeight = 64.8523.dp,
            viewportWidth = 89.1014f,
            viewportHeight = 64.8523f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M1.6918 64.8523L13.8164 36.0917H0V0H39.4753V36.0917L20.5836 64.8523H1.6918ZM51.3179 64.8523L63.4425 36.0917H49.6261V0H89.1014V36.0917L70.2096 64.8523H51.3179Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val exitOutRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_exit_out_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M8.356,12C8.356,11.519 8.747,11.129 9.228,11.129L19.129,11.129C19.61,11.129 20,11.519 20,12C20,12.481 19.61,12.871 19.129,12.871L9.228,12.871C8.747,12.871 8.356,12.481 8.356,12Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M19.744,12.616C19.415,12.967 18.872,12.975 18.531,12.635L14.967,9.071C14.627,8.731 14.619,8.171 14.949,7.82C15.278,7.469 15.821,7.46 16.162,7.801L19.726,11.365C20.066,11.705 20.074,12.265 19.744,12.616Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M19.744,11.384C20.074,11.735 20.066,12.295 19.726,12.635L16.162,16.199C15.821,16.539 15.278,16.531 14.949,16.18C14.619,15.829 14.627,15.269 14.967,14.929L18.532,11.365C18.872,11.025 19.415,11.033 19.744,11.384Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.04,5.743C6.771,5.743 5.743,6.771 5.743,8.04V15.96C5.743,17.229 6.771,18.257 8.04,18.257H11.208C11.689,18.257 12.079,18.647 12.079,19.129C12.079,19.61 11.689,20 11.208,20H8.04C5.809,20 4,18.191 4,15.96V8.04C4,5.809 5.809,4 8.04,4H11.208C11.689,4 12.079,4.39 12.079,4.871C12.079,5.352 11.689,5.743 11.208,5.743H8.04Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val expertFilled16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_expert_filled_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10.853,2.117C11.518,2.117 12.139,2.448 12.511,2.999L14.528,5.992C14.782,6.371 14.749,6.873 14.447,7.214L8.748,13.645C8.351,14.094 7.649,14.094 7.251,13.645L1.553,7.214C1.251,6.873 1.218,6.371 1.473,5.992L3.489,2.999C3.86,2.448 4.483,2.117 5.148,2.117H10.853ZM3.616,5.57V6.87H12.384V5.57H3.616Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val expertOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_expert_outline_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M5.449,1.741C4.551,1.741 3.711,2.187 3.21,2.932L1.428,5.577C0.995,6.22 1.051,7.075 1.565,7.655L6.727,13.48C7.404,14.243 8.596,14.243 9.272,13.48L14.434,7.655C14.948,7.075 15.005,6.22 14.572,5.577L12.79,2.932C12.288,2.187 11.448,1.741 10.55,1.741H5.449ZM10.55,3.141C10.983,3.141 11.387,3.356 11.628,3.714L13.411,6.36C13.487,6.473 13.478,6.624 13.387,6.726L8.224,12.551C8.105,12.686 7.894,12.686 7.775,12.551L2.613,6.726C2.522,6.624 2.513,6.473 2.589,6.36L4.371,3.714C4.612,3.356 5.016,3.141 5.449,3.141H10.55Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.888,5.432V6.833H4.113V5.432H11.888Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val eyeCloseRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_eye_close_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12,13C9.853,13 7.946,12.333 6.597,11.302C5.408,10.393 4.685,9.233 4.531,7.999C4.497,7.725 4.276,7.5 4,7.5H3C2.724,7.5 2.498,7.724 2.521,8C2.682,9.962 3.788,11.671 5.382,12.891C5.612,13.066 5.853,13.233 6.103,13.389L4.75,15.732C4.612,15.971 4.694,16.277 4.933,16.415L5.799,16.915C6.038,17.053 6.344,16.971 6.482,16.732L7.902,14.272C8.861,14.633 9.906,14.869 11,14.959V17.5C11,17.776 11.224,18 11.5,18H12.5C12.776,18 13,17.776 13,17.5V14.959C14.228,14.858 15.392,14.573 16.444,14.135L17.75,16.397C17.888,16.636 18.194,16.718 18.433,16.58L19.299,16.08C19.538,15.942 19.62,15.636 19.482,15.397L18.206,13.187C18.347,13.092 18.484,12.993 18.618,12.891C20.212,11.671 21.318,9.962 21.48,8C21.502,7.724 21.276,7.5 21,7.5H20C19.724,7.5 19.503,7.725 19.469,7.999C19.315,9.233 18.592,10.393 17.403,11.302C16.054,12.333 14.147,13 12,13Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val eyeRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_eye_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M6.857,16.259C5.158,15.163 3.677,13.659 2.738,12C3.677,10.341 5.158,8.837 6.857,7.741C8.621,6.603 10.468,6 12,6C13.532,6 15.38,6.603 17.143,7.741C18.842,8.837 20.323,10.341 21.262,12C20.323,13.659 18.842,15.163 17.143,16.259C15.38,17.397 13.532,18 12,18C10.468,18 8.621,17.397 6.857,16.259ZM0.893,12.797C0.623,12.3 0.623,11.7 0.893,11.203C3.121,7.093 7.974,4 12,4C16.027,4 20.879,7.093 23.107,11.203C23.377,11.7 23.377,12.3 23.107,12.797C20.879,16.907 16.027,20 12,20C7.974,20 3.121,16.907 0.893,12.797ZM14.2,12C14.2,13.215 13.215,14.2 12,14.2C10.785,14.2 9.8,13.215 9.8,12C9.8,10.785 10.785,9.8 12,9.8C13.215,9.8 14.2,10.785 14.2,12ZM16,12C16,14.209 14.209,16 12,16C9.791,16 8,14.209 8,12C8,9.791 9.791,8 12,8C14.209,8 16,9.791 16,12Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val flashAutoFill24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_flash_auto_fill_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13.177,2.546C13.485,2.168 14.095,2.437 14.024,2.919L13.101,9.151C13.058,9.441 13.283,9.701 13.576,9.702H18.612C19.016,9.702 19.24,10.171 18.984,10.485L10.04,21.454C9.731,21.831 9.122,21.563 9.193,21.08L10.116,14.849C10.159,14.559 9.934,14.299 9.641,14.298H4.605C4.201,14.298 3.977,13.828 4.233,13.515L13.177,2.546Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.24,14.806C18.654,14.806 18.99,15.142 18.99,15.556V21.615H17.69V20.139H15.1L13.934,21.615H12.278L14.269,19.093L17.332,15.1L17.389,15.035C17.529,14.89 17.723,14.806 17.927,14.806H18.24ZM16.103,18.839H17.69V16.768L16.103,18.839Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val flashFilled16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_flash_filled_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13.401,6.951L6.941,14.873C6.684,15.188 6.176,14.964 6.235,14.562L6.887,10.16C6.923,9.919 6.736,9.702 6.492,9.702H2.909C2.572,9.702 2.386,9.31 2.599,9.049L9.059,1.127C9.316,0.812 9.824,1.036 9.765,1.438L9.113,5.839C9.077,6.081 9.264,6.298 9.508,6.298H13.091C13.428,6.298 13.614,6.69 13.401,6.951Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val flashOffFill24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_flash_off_fill_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13.676,17.725L10.343,21.813C10.035,22.191 9.424,21.921 9.495,21.439L10.46,14.925C10.503,14.635 10.279,14.375 9.986,14.375H4.731C4.326,14.375 4.103,13.906 4.359,13.592L7.118,10.206L13.676,17.725ZM13.658,2.187C13.966,1.809 14.576,2.078 14.504,2.561L13.539,9.074C13.496,9.364 13.722,9.624 14.015,9.624H19.27C19.674,9.625 19.897,10.095 19.642,10.408L16.881,13.793L10.325,6.275L13.658,2.187Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M19.392,19.258L18.789,19.785L18.186,20.31L4.609,4.741L5.814,3.69L19.392,19.258Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val flashOnFill24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_flash_on_fill_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M19.643,10.408L10.342,21.815C10.034,22.193 9.424,21.924 9.495,21.441L10.46,14.926C10.503,14.636 10.278,14.375 9.985,14.375L4.729,14.375C4.325,14.375 4.101,13.906 4.357,13.592L13.658,2.185C13.966,1.807 14.576,2.076 14.505,2.559L13.54,9.074C13.497,9.364 13.722,9.624 14.015,9.624L19.271,9.624C19.676,9.624 19.899,10.094 19.643,10.408Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val flashOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_flash_outline_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M7.206,14.799C6.605,15.324 5.608,14.827 5.733,13.979L6.301,10.146H3.378C2.619,10.146 2.2,9.266 2.679,8.678L8.679,1.32C9.257,0.612 10.401,1.116 10.267,2.021L9.7,5.854L12.623,5.854C13.381,5.854 13.799,6.734 13.32,7.322L7.321,14.68L7.206,14.799ZM11.57,7.253L9.12,7.253C8.571,7.253 8.149,6.765 8.229,6.222L8.616,3.614L4.43,8.747L6.88,8.747C7.429,8.747 7.851,9.235 7.77,9.778L7.385,12.388L11.57,7.253Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val folderOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_folder_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M1.2,11.418V6.724C1.2,6.47 1.2,6.296 1.214,6.128C1.297,5.144 1.761,4.232 2.505,3.584C2.632,3.473 2.773,3.371 2.979,3.22C3.113,3.122 3.204,3.055 3.3,2.993C3.853,2.635 4.49,2.426 5.148,2.388C5.262,2.382 5.376,2.383 5.542,2.383H7.912C8.635,2.383 9.132,2.377 9.613,2.499C9.766,2.537 9.916,2.585 10.063,2.642C10.525,2.823 10.927,3.117 11.515,3.537L12.086,3.944C12.193,4.02 12.251,4.062 12.296,4.091C12.334,4.115 12.339,4.116 12.332,4.113C12.34,4.116 12.349,4.119 12.357,4.121C12.363,4.122 12.376,4.123 12.4,4.124C12.454,4.126 12.525,4.127 12.656,4.127C13.737,4.127 14.611,4.126 15.309,4.2C16.021,4.275 16.643,4.436 17.188,4.82C17.545,5.071 17.855,5.382 18.106,5.738C18.491,6.284 18.652,6.906 18.728,7.619C18.801,8.317 18.8,9.191 18.8,10.271V11.418C18.8,12.525 18.801,13.42 18.724,14.134C18.645,14.863 18.476,15.498 18.074,16.052C17.84,16.374 17.556,16.657 17.233,16.891C16.68,17.294 16.045,17.463 15.315,17.542C14.601,17.619 13.707,17.618 12.6,17.618H7.4C6.294,17.618 5.399,17.619 4.685,17.542C3.955,17.463 3.32,17.294 2.767,16.891C2.444,16.657 2.16,16.374 1.926,16.052C1.524,15.498 1.355,14.863 1.276,14.134C1.199,13.42 1.2,12.525 1.2,11.418ZM2.8,11.418C2.8,12.56 2.801,13.353 2.867,13.961C2.931,14.554 3.049,14.876 3.22,15.111C3.355,15.298 3.52,15.462 3.707,15.597C3.942,15.768 4.264,15.887 4.856,15.951C5.464,16.017 6.258,16.017 7.4,16.017H12.6C13.742,16.017 14.536,16.017 15.144,15.951C15.736,15.887 16.058,15.768 16.293,15.597C16.48,15.462 16.645,15.298 16.78,15.111C16.951,14.876 17.069,14.554 17.133,13.961C17.199,13.353 17.2,12.56 17.2,11.418V10.271C17.2,9.156 17.199,8.381 17.136,7.787C17.074,7.207 16.962,6.891 16.799,6.66C16.653,6.454 16.473,6.274 16.267,6.129C16.035,5.966 15.72,5.852 15.14,5.791C14.545,5.728 13.772,5.727 12.656,5.727C12.446,5.727 12.208,5.733 11.974,5.675C11.895,5.655 11.817,5.63 11.741,5.6C11.517,5.511 11.327,5.369 11.156,5.247L10.585,4.839C9.933,4.373 9.715,4.224 9.48,4.133C9.396,4.1 9.309,4.072 9.221,4.05C8.977,3.988 8.713,3.982 7.912,3.982H5.542C5.358,3.982 5.297,3.983 5.239,3.986C4.858,4.008 4.489,4.128 4.169,4.336C4.121,4.367 4.071,4.403 3.923,4.512C3.696,4.678 3.62,4.734 3.556,4.79C3.125,5.165 2.856,5.693 2.808,6.262C2.8,6.348 2.8,6.442 2.8,6.724V11.418Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M15.491,8.118V9.718H4.509V8.118H15.491Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val fontSizeLarge: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_font_size_large",
            defaultWidth = 16.dp,
            defaultHeight = 26.dp,
            viewportWidth = 16f,
            viewportHeight = 26f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M2.683,23.183C1.834,23.183 1.286,22.688 1.286,21.904C1.286,21.69 1.34,21.389 1.469,21.056L5.97,8.82C6.346,7.789 6.969,7.316 7.979,7.316C8.999,7.316 9.622,7.768 10.009,8.81L14.521,21.056C14.649,21.41 14.703,21.657 14.703,21.904C14.703,22.656 14.123,23.183 13.317,23.183C12.565,23.183 12.146,22.839 11.899,22.012L10.879,19.047H5.089L4.068,21.99C3.811,22.828 3.402,23.183 2.683,23.183ZM5.744,16.855H10.202L8,10.238H7.925L5.744,16.855Z").toNodes(),
                fill = SolidColor(Color(0xFF1E1E1E)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val fontSizeSmall: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_font_size_small",
            defaultWidth = 16.dp,
            defaultHeight = 26.dp,
            viewportWidth = 16f,
            viewportHeight = 26f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M4.016,23.133C3.398,23.133 3,22.773 3,22.203C3,22.047 3.039,21.828 3.133,21.586L6.406,12.688C6.68,11.938 7.133,11.594 7.867,11.594C8.609,11.594 9.063,11.922 9.344,12.68L12.625,21.586C12.719,21.844 12.758,22.023 12.758,22.203C12.758,22.75 12.336,23.133 11.75,23.133C11.203,23.133 10.898,22.883 10.719,22.281L9.977,20.125H5.766L5.023,22.266C4.836,22.875 4.539,23.133 4.016,23.133ZM6.242,18.531H9.484L7.883,13.719H7.828L6.242,18.531Z").toNodes(),
                fill = SolidColor(Color(0xFF1E1E1E)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val fullscreenOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_fullscreen_outline",
            defaultWidth = 16.dp,
            defaultHeight = 17.dp,
            viewportWidth = 16f,
            viewportHeight = 17f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M2.407,13.13L6.06,9.478L6.98,10.398L3.24,14.137C3.329,14.138 3.429,14.139 3.542,14.139H8.045L8.046,14.781V15.423L3.542,15.422C3.186,15.422 2.867,15.424 2.607,15.396C2.335,15.366 2.052,15.299 1.794,15.112C1.656,15.011 1.535,14.89 1.434,14.752C1.247,14.494 1.179,14.211 1.15,13.939C1.122,13.679 1.124,13.36 1.124,13.004V8.5H2.407V13.13ZM12.504,1.616C12.86,1.616 13.179,1.614 13.44,1.642C13.712,1.672 13.998,1.74 14.256,1.928C14.395,2.029 14.518,2.152 14.619,2.291C14.807,2.549 14.875,2.833 14.905,3.106C14.933,3.367 14.931,3.687 14.931,4.042V8.545L14.282,8.546H13.631V4.042C13.631,3.933 13.63,3.835 13.629,3.748L9.898,7.48L8.978,6.56L12.622,2.916H8.001V1.615L12.504,1.616Z").toNodes(),
                fill = SolidColor(Color(0xFF7F8287)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val globeLanguageOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_globe_language_outline_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M8.001,0.451C12.17,0.451 15.55,3.831 15.55,8C15.55,12.169 12.17,15.549 8.001,15.549C3.832,15.549 0.451,12.17 0.451,8C0.451,3.831 3.832,0.451 8.001,0.451ZM6.268,8.701C6.324,10.249 6.565,11.613 6.917,12.61C7.12,13.185 7.347,13.6 7.565,13.858C7.776,14.105 7.922,14.144 7.992,14.147C7.995,14.147 7.997,14.148 8,14.148C8.067,14.148 8.216,14.116 8.436,13.858C8.654,13.6 8.881,13.185 9.084,12.61C9.436,11.613 9.677,10.249 9.733,8.701H6.268ZM11.134,8.701C11.077,10.375 10.818,11.905 10.404,13.077C10.318,13.321 10.223,13.554 10.119,13.774C12.261,12.988 13.843,11.044 14.108,8.701H11.134ZM1.893,8.701C2.158,11.043 3.74,12.986 5.881,13.773C5.777,13.554 5.683,13.32 5.597,13.077C5.183,11.905 4.924,10.375 4.867,8.701H1.893ZM8,1.851C7.933,1.851 7.784,1.885 7.565,2.142C7.347,2.4 7.12,2.815 6.917,3.39C6.565,4.387 6.324,5.752 6.268,7.3H9.733C9.677,5.752 9.436,4.387 9.084,3.39C8.881,2.815 8.654,2.4 8.436,2.142C8.216,1.884 8.067,1.851 8,1.851ZM10.119,2.225C10.223,2.445 10.318,2.679 10.404,2.923C10.818,4.096 11.078,5.625 11.135,7.3H14.108C13.843,4.957 12.261,3.011 10.119,2.225ZM5.881,2.226C3.739,3.013 2.158,4.957 1.893,7.3H4.867C4.924,5.625 5.183,4.096 5.597,2.923C5.683,2.679 5.777,2.446 5.881,2.226Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val globeLanguageOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_globe_language_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10,1.691C14.589,1.691 18.31,5.412 18.31,10.001C18.31,14.59 14.589,18.312 10,18.312C5.41,18.311 1.69,14.59 1.69,10.001C1.69,5.412 5.41,1.692 10,1.691ZM3.232,10.751C3.529,13.465 5.42,15.698 7.949,16.496C7.743,16.141 7.565,15.738 7.412,15.304C6.979,14.079 6.705,12.492 6.632,10.751H3.232ZM13.367,10.751C13.294,12.492 13.021,14.079 12.588,15.304C12.435,15.737 12.256,16.14 12.05,16.495C14.579,15.697 16.471,13.465 16.768,10.751H13.367ZM8.134,10.751C8.206,12.353 8.459,13.763 8.827,14.804C9.046,15.425 9.29,15.873 9.527,16.151C9.763,16.43 9.925,16.467 10,16.467C10.074,16.467 10.236,16.43 10.473,16.151C10.71,15.873 10.954,15.425 11.173,14.804C11.541,13.763 11.794,12.353 11.866,10.751H8.134ZM10,3.191C9.925,3.192 9.763,3.23 9.527,3.508C9.29,3.786 9.046,4.235 8.827,4.855C8.433,5.97 8.17,7.508 8.122,9.251H11.878C11.83,7.508 11.567,5.97 11.173,4.855C10.954,4.235 10.71,3.786 10.473,3.508C10.236,3.229 10.074,3.191 10,3.191ZM12.27,3.58C12.386,3.824 12.492,4.084 12.588,4.355C13.049,5.66 13.33,7.376 13.378,9.251H16.768C16.479,6.617 14.689,4.435 12.27,3.58ZM7.729,3.58C5.311,4.435 3.521,6.617 3.232,9.251H6.622C6.67,7.376 6.951,5.66 7.412,4.355C7.508,4.084 7.613,3.824 7.729,3.58Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val globeLanguageRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_globe_language_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12,18.996C15.864,18.996 18.996,15.863 18.996,12C18.996,8.137 15.864,5.005 12,5.005C8.137,5.005 5.005,8.137 5.005,12C5.005,15.863 8.137,18.996 12,18.996ZM12,20.55C16.722,20.55 20.55,16.722 20.55,12C20.55,7.278 16.722,3.45 12,3.45C7.278,3.45 3.45,7.278 3.45,12C3.45,16.722 7.278,20.55 12,20.55Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M19.773,12.777H4.227V11.223H19.773V12.777Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M10.847,6.773C10.364,8.082 10.018,9.932 10.018,12C10.018,14.068 10.364,15.918 10.847,17.227C11.09,17.885 11.355,18.371 11.599,18.676C11.837,18.972 11.967,18.994 11.967,18.995C11.967,18.996 11.965,18.996 11.961,18.996V20.55C11.283,20.55 10.753,20.105 10.387,19.648C10,19.167 9.665,18.514 9.389,17.765C8.834,16.261 8.464,14.225 8.464,12C8.464,9.775 8.834,7.739 9.389,6.235C9.665,5.486 10,4.833 10.387,4.352C10.753,3.895 11.283,3.45 11.961,3.45V5.005C11.965,5.005 11.967,5.004 11.967,5.005C11.967,5.006 11.837,5.028 11.599,5.324C11.355,5.63 11.09,6.115 10.847,6.773Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M13.075,6.773C13.558,8.082 13.905,9.932 13.905,12C13.905,14.068 13.558,15.918 13.075,17.227C12.832,17.885 12.568,18.371 12.323,18.676C12.086,18.972 11.956,18.994 11.956,18.995C11.956,18.996 11.958,18.996 11.961,18.996V20.55C12.639,20.55 13.17,20.105 13.536,19.648C13.922,19.167 14.257,18.514 14.534,17.765C15.089,16.261 15.459,14.225 15.459,12C15.459,9.775 15.089,7.739 14.534,6.235C14.257,5.486 13.922,4.833 13.536,4.352C13.17,3.895 12.639,3.45 11.961,3.45V5.005C11.958,5.005 11.956,5.004 11.956,5.005C11.956,5.006 12.086,5.028 12.323,5.324C12.568,5.63 12.832,6.115 13.075,6.773Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val helpOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_help_outline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M18.384,12C18.384,8.474 15.526,5.616 12,5.616C8.474,5.616 5.616,8.474 5.616,12C5.616,15.526 8.474,18.384 12,18.384C15.526,18.384 18.384,15.526 18.384,12ZM19.94,12C19.94,16.385 16.385,19.94 12,19.94C7.615,19.94 4.06,16.385 4.06,12C4.06,7.615 7.615,4.06 12,4.06C16.385,4.06 19.94,7.615 19.94,12Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.027,13.83C11.027,13.151 11.197,12.273 11.904,11.695C12.119,11.519 12.338,11.374 12.531,11.25C12.737,11.119 12.892,11.025 13.046,10.919C13.339,10.716 13.459,10.575 13.514,10.416C13.606,10.15 13.561,9.85 13.383,9.613C13.213,9.387 12.879,9.168 12.304,9.168C10.964,9.168 10.439,9.99 10.439,10.661H8.939C8.939,9.11 10.189,7.668 12.304,7.668C13.294,7.668 14.095,8.066 14.581,8.71C15.057,9.342 15.188,10.164 14.931,10.906C14.719,11.521 14.278,11.891 13.898,12.154C13.715,12.28 13.504,12.409 13.339,12.515C13.16,12.629 13,12.736 12.854,12.856C12.657,13.018 12.527,13.34 12.527,13.83V13.859H11.027V13.83Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M12.515,14.595V16.071H11.015V14.595H12.515Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val infoOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_info_outline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12.81,16.195H11.21V11.089H12.81V16.195Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M12.81,9.691H11.21V7.804H12.81V9.691Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M12,4.063C16.384,4.063 19.938,7.616 19.938,12C19.938,16.384 16.384,19.938 12,19.938C7.616,19.938 4.063,16.384 4.063,12C4.063,7.616 7.616,4.063 12,4.063ZM12,5.662C8.5,5.662 5.662,8.5 5.662,12C5.662,15.5 8.5,18.338 12,18.338C15.5,18.338 18.338,15.5 18.338,12C18.338,8.5 15.5,5.662 12,5.662Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val infoOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_info_outline_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M7.24,12.25L7.24,7.26L8.76,7.26L8.76,12.25L7.24,12.25Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M7.1,5.81L7.1,3.925L8.91,3.925L8.91,5.81L7.1,5.81Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M14.55,8C14.55,4.383 11.618,1.45 8,1.45C4.383,1.45 1.45,4.383 1.45,8C1.45,11.618 4.383,14.55 8,14.55C11.618,14.55 14.55,11.618 14.55,8ZM15.95,8C15.95,12.391 12.391,15.95 8,15.95C3.609,15.95 0.05,12.391 0.05,8C0.05,3.609 3.609,0.05 8,0.05C12.391,0.05 15.95,3.609 15.95,8Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val keyboardOutline26: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_keyboard_outline_26",
            defaultWidth = 26.dp,
            defaultHeight = 26.dp,
            viewportWidth = 26f,
            viewportHeight = 26f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M7.752,18.09H18.25").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 1.87f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M7.182,13.606H9.582").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 2.1f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M7.182,9.164H9.582").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 2.1f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.801,13.606H14.201").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 2.1f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.801,9.164H14.201").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 2.1f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.418,13.606H18.818").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 2.1f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.418,9.164H18.818").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 2.1f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M13.001,12.999m-11.331,0a11.331,11.331 0,1 1,22.662 0a11.331,11.331 0,1 1,-22.662 0").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val keyboardVoice24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_keyboard_voice_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12,15C13.66,15 14.99,13.66 14.99,12L15,6C15,4.34 13.66,3 12,3C10.34,3 9,4.34 9,6V12C9,13.66 10.34,15 12,15ZM10.8,5.9C10.8,5.24 11.34,4.7 12,4.7C12.66,4.7 13.2,5.24 13.2,5.9L13.19,12.1C13.19,12.76 12.66,13.3 12,13.3C11.34,13.3 10.8,12.76 10.8,12.1V5.9ZM17.3,12C17.3,15 14.76,17.1 12,17.1C9.24,17.1 6.7,15 6.7,12H5.85H5C5,15.41 7.72,18.23 11,18.72V22H13V18.72C16.28,18.24 19,15.42 19,12H17.3Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val lassoSelectOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_lasso_select_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M4.836,5.628C7.444,3.073 11.502,3.029 15.045,4.865L13.776,6.236C11.013,4.964 8.137,5.151 6.335,6.694L6.095,6.914C3.196,9.752 3.634,15.422 7.753,19.63L6.467,20.89C1.974,16.3 0.938,9.445 4.836,5.628Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.966,7.989C20.404,9.685 21.237,11.63 21.442,13.488C21.672,15.58 21.107,17.628 19.625,18.99C18.309,20.2 16.495,20.601 14.701,20.368C12.966,20.141 11.188,19.317 9.722,17.971L10.946,16.651C12.15,17.755 13.586,18.406 14.934,18.581C16.334,18.764 17.572,18.433 18.406,17.666C19.368,16.783 19.835,15.357 19.652,13.686C19.495,12.266 18.868,10.718 17.734,9.32L18.966,7.989Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M9.292,13.943L7.971,16.466L8.311,16.768L10.706,15.2L17.964,6.91L16.533,5.673L9.292,13.943Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M17.588,4.468L19.018,5.706L20.218,4.335L18.805,3.078L17.588,4.468Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val letterRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_letter_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M6.147,4.75C4.575,4.75 3.3,6.048 3.3,7.65V16.35C3.3,17.952 4.575,19.25 6.147,19.25H17.853C19.425,19.25 20.7,17.952 20.7,16.35V7.65C20.7,6.048 19.425,4.75 17.853,4.75H6.147ZM6.147,6.361C5.448,6.361 4.882,6.938 4.882,7.65V7.983L12,11.839L19.118,7.983V7.65C19.118,6.938 18.551,6.361 17.853,6.361H6.147ZM19.118,9.917L12.417,13.546C12.392,13.56 12.366,13.573 12.34,13.585C12.238,13.629 12.132,13.652 12.026,13.655C11.885,13.66 11.74,13.629 11.606,13.559C11.601,13.556 11.595,13.553 11.59,13.55L4.882,9.917V16.35C4.882,17.062 5.448,17.639 6.147,17.639H17.853C18.551,17.639 19.118,17.062 19.118,16.35V9.917Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val likeFill: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_like_fill",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M16.46,15.133L17.567,12.216C17.792,11.623 17.905,11.326 17.95,11.092C18.247,9.549 17.194,8.068 15.612,7.803C15.372,7.763 15.048,7.763 14.401,7.763H13.316C12.838,7.763 12.599,7.763 12.456,7.637C12.41,7.596 12.373,7.548 12.345,7.494C12.258,7.326 12.322,7.101 12.449,6.65L12.994,4.717L13.005,4.676C13.234,3.837 12.991,2.942 12.366,2.326L12.335,2.295L12.313,2.274C11.891,1.876 11.211,1.917 10.842,2.363L10.823,2.386L6.493,7.769C6.3,8.009 6.147,8.281 6.147,8.586V14.947C6.147,16.633 7.544,18 9.268,18L12.246,18C13.585,18 14.255,18 14.802,17.717C14.981,17.624 15.148,17.511 15.3,17.379C15.761,16.975 15.994,16.361 16.46,15.133Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M4.577,15.435C4.577,16.978 5.826,17.878 5.826,17.878H4.303C3.031,17.878 2,16.869 2,15.625V10.359C2,9.115 3.031,8.107 4.303,8.107H4.577V15.435Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val likeOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_like_outline",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10.298,2.32C10.877,1.605 11.915,1.499 12.624,2.049L12.761,2.169L12.767,2.174L12.787,2.194L12.789,2.197L12.818,2.225L12.821,2.229C13.587,3 13.886,4.119 13.606,5.169L13.604,5.174L13.593,5.212V5.214L13.089,7.038C13.079,7.073 13.07,7.107 13.062,7.137C13.094,7.137 13.129,7.138 13.167,7.138H14.17C14.732,7.138 15.115,7.136 15.416,7.187C17.29,7.508 18.539,9.3 18.188,11.169C18.131,11.469 17.996,11.826 17.8,12.352L16.776,15.106C16.566,15.673 16.391,16.145 16.212,16.519C16.027,16.906 15.815,17.242 15.5,17.523C15.315,17.688 15.111,17.83 14.892,17.946C14.518,18.143 14.128,18.225 13.701,18.263C13.287,18.3 12.784,18.301 12.179,18.301H6.657C5.809,18.301 5.112,18.301 4.554,18.241C3.982,18.179 3.466,18.045 3.014,17.716C2.754,17.528 2.526,17.3 2.337,17.041C2.008,16.589 1.875,16.073 1.813,15.502C1.752,14.944 1.753,14.248 1.753,13.4V11.617C1.753,10.905 1.746,10.392 1.872,9.953C2.163,8.941 2.955,8.149 3.967,7.859C4.406,7.733 4.92,7.741 5.632,7.741C5.851,7.741 5.878,7.739 5.897,7.736C5.975,7.723 6.047,7.688 6.106,7.634C6.119,7.622 6.137,7.602 6.273,7.43L10.274,2.35L10.276,2.349L10.293,2.327L10.298,2.32ZM7.292,14.669C7.292,15.845 8.247,16.801 9.427,16.801H12.179C12.811,16.801 13.236,16.799 13.568,16.769C13.885,16.741 14.059,16.69 14.191,16.62C14.303,16.561 14.407,16.488 14.501,16.404C14.613,16.304 14.721,16.16 14.858,15.873C15.002,15.573 15.15,15.175 15.37,14.583L16.394,11.83C16.614,11.237 16.686,11.033 16.713,10.891C16.909,9.848 16.212,8.845 15.163,8.666C15.02,8.641 14.803,8.638 14.17,8.638H13.167C12.965,8.638 12.747,8.64 12.566,8.619C12.375,8.596 12.104,8.538 11.869,8.326C11.759,8.226 11.668,8.106 11.601,7.973C11.458,7.691 11.474,7.414 11.503,7.223C11.531,7.043 11.59,6.833 11.644,6.638L12.147,4.814L12.157,4.777C12.298,4.245 12.146,3.677 11.757,3.286L11.734,3.263L11.72,3.249L11.691,3.225C11.618,3.18 11.52,3.195 11.464,3.264L11.451,3.278L11.452,3.279L7.451,8.359C7.399,8.426 7.346,8.492 7.292,8.556V14.669ZM3.253,13.4C3.253,14.281 3.254,14.882 3.304,15.34C3.352,15.784 3.438,16.005 3.55,16.159C3.646,16.291 3.762,16.407 3.895,16.503C4.049,16.615 4.27,16.701 4.715,16.75C5.143,16.796 5.695,16.798 6.483,16.799C6.049,16.2 5.792,15.465 5.792,14.669V9.239C5.739,9.239 5.685,9.241 5.632,9.241C4.817,9.241 4.566,9.248 4.381,9.301C3.865,9.449 3.462,9.852 3.314,10.367C3.26,10.552 3.253,10.802 3.253,11.617V13.4Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val linkEmptyOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_link_empty_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 32f,
            viewportHeight = 32f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M4.886,21.189C4.686,22.692 5.149,24.265 6.283,25.434L6.562,25.716C7.73,26.849 9.305,27.31 10.808,27.111L12.601,28.904C10.057,29.817 7.11,29.276 5.048,27.277C4.938,27.17 4.829,27.061 4.722,26.951C2.722,24.889 2.179,21.94 3.092,19.396L4.886,21.189Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M20.267,21.909C19.983,22.384 19.643,22.834 19.242,23.247C19.202,23.288 19.155,23.336 19.08,23.411L15.378,27.114C15.128,27.364 14.866,27.599 14.586,27.815L13.031,26.26C13.265,26.1 13.49,25.92 13.7,25.716L17.681,21.733C18.098,21.304 18.424,20.819 18.661,20.303L20.267,21.909Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M12.956,14.606C13.112,15.62 13.569,16.598 14.337,17.39C14.8,17.867 15.316,18.283 15.881,18.567L14.286,20.162C13.726,19.809 13.243,19.375 12.778,18.908C11.068,17.147 10.421,14.738 10.838,12.487L12.956,14.606Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M17.698,11.803C18.253,12.15 18.768,12.594 19.242,13.083C21.001,14.897 21.63,17.396 21.135,19.703L19.101,17.669C18.993,16.551 18.521,15.462 17.681,14.596C17.21,14.12 16.709,13.699 16.103,13.4L17.698,11.803Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.036,13.471C7.955,14.431 8.012,15.407 8.215,16.369L6.283,18.298C6.079,18.508 5.897,18.732 5.737,18.966L4.181,17.41C4.396,17.131 4.634,16.87 4.883,16.621L8.036,13.471Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M27.657,14.789C27.487,14.994 27.306,15.189 27.118,15.377L23.945,18.549C24.007,17.601 23.935,16.64 23.722,15.696L25.716,13.703C25.859,13.556 25.989,13.401 26.111,13.242L27.657,14.789Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M19.14,3.196C21.741,2.147 24.822,2.658 26.955,4.728C27.064,4.834 27.171,4.941 27.277,5.049C29.346,7.183 29.855,10.263 28.807,12.864L27.066,11.123C27.378,9.528 26.929,7.812 25.716,6.561L25.442,6.287C24.191,5.074 22.475,4.624 20.88,4.937L19.14,3.196Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.761,5.893C18.603,6.014 18.45,6.146 18.303,6.288L14.337,10.254C13.874,10.731 13.522,11.276 13.282,11.856L11.659,10.233C11.96,9.701 12.333,9.198 12.778,8.739C12.817,8.699 12.862,8.652 12.936,8.579L16.629,4.889C16.817,4.7 17.01,4.518 17.216,4.347L18.761,5.893Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val linkOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_link_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M6.726,9.27L4.559,11.438C3.454,12.543 3.454,14.335 4.559,15.441C5.664,16.546 7.457,16.546 8.563,15.441L10.73,13.274L11.79,14.334L9.623,16.501C7.932,18.192 5.19,18.193 3.498,16.501C1.807,14.81 1.807,12.068 3.498,10.376L5.665,8.209L6.726,9.27ZM12.999,8.061L8.061,12.999L7,11.939L11.939,7L12.999,8.061ZM10.376,3.498C12.068,1.807 14.811,1.807 16.502,3.498C18.193,5.19 18.193,7.932 16.502,9.623L14.334,11.79L13.274,10.73L15.441,8.563C16.546,7.457 16.546,5.664 15.441,4.559C14.335,3.454 12.543,3.454 11.438,4.559L9.27,6.727L8.209,5.665L10.376,3.498Z").toNodes(),
                fill = SolidColor(Color(0xFF0F1115)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val listPenRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_list_pen_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M8.472,7.92C8.472,7.528 8.79,7.21 9.182,7.21H14.863C15.255,7.21 15.573,7.528 15.573,7.92C15.573,8.312 15.255,8.63 14.863,8.63H9.182C8.79,8.63 8.472,8.312 8.472,7.92Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.472,11.234C8.472,10.842 8.79,10.524 9.182,10.524H14.863C15.255,10.524 15.573,10.842 15.573,11.234C15.573,11.626 15.255,11.944 14.863,11.944H9.182C8.79,11.944 8.472,11.626 8.472,11.234Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M7.762,3.453C6.193,3.453 4.922,4.701 4.922,6.241V17.705C4.922,19.246 6.193,20.494 7.762,20.494H11.381C11.484,20.183 11.558,19.971 11.642,19.764C11.754,19.485 11.878,19.212 12.015,18.945H7.762C7.065,18.945 6.5,18.39 6.5,17.705V6.241C6.5,5.557 7.065,5.002 7.762,5.002H16.283C16.98,5.002 17.545,5.557 17.545,6.241V12.776L19.048,11.293C19.074,11.267 19.099,11.242 19.123,11.219V6.241C19.123,4.701 17.851,3.453 16.283,3.453H7.762Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M13.076,20.243L13.073,20.253L13.073,20.254C13.024,20.403 13.165,20.545 13.314,20.496L13.318,20.495L13.318,20.495C13.374,20.477 13.402,20.468 13.427,20.459C14.418,20.127 15.323,19.576 16.074,18.848C16.093,18.83 16.111,18.812 16.149,18.775L16.149,18.774L20.338,14.652C20.57,14.424 20.685,14.31 20.729,14.178C20.767,14.062 20.768,13.936 20.73,13.82C20.688,13.688 20.573,13.573 20.343,13.343L20.311,13.311C20.085,13.085 19.972,12.971 19.841,12.929C19.726,12.891 19.602,12.89 19.487,12.927C19.355,12.969 19.241,13.081 19.013,13.306L14.821,17.431L14.814,17.438C14.02,18.221 13.424,19.183 13.076,20.243ZM19.973,19.446L17,19.446L15.5,20.5L19.973,20.5C20.264,20.5 20.5,20.264 20.5,19.973C20.5,19.682 20.264,19.446 19.973,19.446Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val loading: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_loading",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 128f,
            viewportHeight = 128f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M64,32.5C46.6,32.5 32.5,46.6 32.5,64C32.5,81.4 46.6,95.5 64,95.5C66.49,95.5 68.5,97.51 68.5,100C68.5,102.49 66.49,104.5 64,104.5C41.63,104.5 23.5,86.37 23.5,64C23.5,41.63 41.63,23.5 64,23.5C66.49,23.5 68.5,25.51 68.5,28C68.5,30.49 66.49,32.5 64,32.5Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val lockFill: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_lock_fill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M4.52,12.767C4.52,11.202 5.789,9.933 7.353,9.933H16.647C18.212,9.933 19.48,11.202 19.48,12.767V17.933C19.48,19.498 18.212,20.767 16.647,20.767H7.353C5.789,20.767 4.52,19.498 4.52,17.933V12.767ZM12.9,14.224C12.9,13.727 12.497,13.325 12,13.325C11.503,13.325 11.1,13.727 11.1,14.224V16.285C11.1,16.782 11.503,17.184 12,17.184C12.497,17.184 12.9,16.782 12.9,16.285V14.224Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M12,4.933C10.62,4.933 9.5,6.053 9.5,7.433V11H7.833V7.433C7.833,5.132 9.699,3.267 12,3.267C14.301,3.267 16.167,5.132 16.167,7.433V11H14.5V7.433C14.5,6.053 13.381,4.933 12,4.933Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val menuOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_menu_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13.19,17.958H3.074V16.157H13.19V17.958ZM20.927,7.843H3.074V6.042H20.927V7.843Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val micFilled20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_mic_filled_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M16.024,10.992L15.794,11.608C15.015,13.695 13.028,15.212 10.657,15.451V16.728H9.344V15.451C6.973,15.212 4.985,13.695 4.206,11.608L3.976,10.992L5.207,10.533L5.436,11.149C6.087,12.891 7.869,14.173 10,14.173C12.132,14.173 13.915,12.891 14.565,11.149L14.794,10.533L16.024,10.992Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M10,3.273C11.862,3.273 13.371,4.782 13.371,6.644V9.631C13.371,11.493 11.862,13.002 10,13.002C8.139,13.002 6.629,11.493 6.629,9.631V6.644C6.629,4.782 8.139,3.273 10,3.273Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val moonRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_moon_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M11.28,21.6C10.222,21.6 9.24,21.405 8.333,21.016C7.431,20.632 6.641,20.095 5.962,19.405C5.283,18.715 4.752,17.916 4.368,17.009C3.989,16.096 3.8,15.117 3.8,14.07C3.8,13.069 3.989,12.114 4.368,11.207C4.752,10.295 5.286,9.488 5.97,8.786C6.655,8.08 7.445,7.54 8.341,7.167C8.569,7.078 8.77,7.047 8.942,7.075C9.115,7.103 9.254,7.175 9.36,7.292C9.56,7.52 9.563,7.84 9.368,8.252C9.234,8.53 9.123,8.9 9.034,9.362C8.945,9.824 8.9,10.306 8.9,10.807C8.9,12.02 9.137,13.058 9.61,13.92C10.083,14.777 10.762,15.437 11.647,15.899C12.532,16.361 13.592,16.591 14.827,16.591C15.295,16.591 15.723,16.555 16.113,16.483C16.502,16.41 16.853,16.31 17.165,16.182C17.348,16.104 17.518,16.074 17.674,16.091C17.83,16.107 17.955,16.16 18.049,16.249C18.15,16.355 18.211,16.491 18.233,16.658C18.255,16.825 18.219,17.02 18.125,17.243C17.724,18.15 17.17,18.929 16.463,19.58C15.757,20.231 14.955,20.729 14.059,21.074C13.169,21.425 12.242,21.6 11.28,21.6ZM11.28,20.014C11.92,20.014 12.515,19.928 13.066,19.755C13.622,19.583 14.129,19.338 14.585,19.021C15.042,18.703 15.437,18.339 15.771,17.927C15.604,17.966 15.406,17.999 15.178,18.027C14.95,18.055 14.708,18.069 14.452,18.069C13.016,18.069 11.769,17.791 10.712,17.234C9.66,16.672 8.848,15.879 8.274,14.855C7.707,13.826 7.423,12.607 7.423,11.199C7.423,10.887 7.44,10.578 7.473,10.272C7.512,9.966 7.568,9.694 7.64,9.454C7.172,9.844 6.769,10.281 6.43,10.765C6.096,11.249 5.837,11.772 5.653,12.334C5.47,12.896 5.378,13.489 5.378,14.112C5.378,14.936 5.525,15.704 5.82,16.416C6.121,17.128 6.538,17.754 7.072,18.294C7.607,18.834 8.233,19.254 8.951,19.555C9.668,19.861 10.445,20.014 11.28,20.014ZM15.988,5.731C16.138,5.731 16.26,5.776 16.355,5.865C16.449,5.948 16.497,6.062 16.497,6.207C16.497,6.357 16.447,6.474 16.347,6.558C16.252,6.641 16.132,6.683 15.988,6.683H13.149C12.993,6.683 12.868,6.638 12.774,6.549C12.679,6.46 12.632,6.346 12.632,6.207C12.632,6.107 12.649,6.018 12.682,5.94C12.721,5.862 12.785,5.762 12.874,5.639L14.852,3.11V3.068H13.099C12.949,3.068 12.824,3.027 12.724,2.943C12.629,2.854 12.582,2.734 12.582,2.584C12.582,2.434 12.629,2.317 12.724,2.234C12.818,2.145 12.943,2.1 13.099,2.1H15.804C15.965,2.1 16.096,2.145 16.196,2.234C16.296,2.317 16.347,2.434 16.347,2.584C16.347,2.673 16.327,2.759 16.288,2.843C16.249,2.926 16.182,3.027 16.088,3.143L14.126,5.689V5.731H15.988ZM19.969,8.978C20.292,8.978 20.454,9.129 20.454,9.429C20.454,9.568 20.409,9.677 20.32,9.755C20.231,9.833 20.114,9.872 19.969,9.872H17.824C17.679,9.872 17.563,9.83 17.473,9.746C17.384,9.663 17.34,9.557 17.34,9.429C17.34,9.335 17.357,9.246 17.39,9.162C17.429,9.073 17.493,8.976 17.582,8.87L18.918,7.159V7.125H17.774C17.635,7.125 17.521,7.083 17.432,7C17.343,6.917 17.298,6.802 17.298,6.658C17.298,6.53 17.343,6.424 17.432,6.341C17.521,6.257 17.635,6.215 17.774,6.215H19.802C19.947,6.215 20.067,6.257 20.161,6.341C20.261,6.424 20.312,6.533 20.312,6.666C20.312,6.761 20.292,6.85 20.253,6.933C20.214,7.011 20.153,7.103 20.069,7.209L18.709,8.945V8.978H19.969ZM16.213,11.858C16.513,11.858 16.664,12 16.664,12.284C16.664,12.412 16.622,12.518 16.538,12.601C16.455,12.679 16.347,12.718 16.213,12.718H14.276C14.143,12.718 14.034,12.679 13.951,12.601C13.867,12.518 13.825,12.415 13.825,12.292C13.825,12.198 13.842,12.114 13.876,12.042C13.909,11.964 13.97,11.875 14.059,11.775L15.194,10.297V10.256H14.235C14.107,10.256 13.998,10.219 13.909,10.147C13.825,10.069 13.784,9.963 13.784,9.83C13.784,9.696 13.825,9.593 13.909,9.521C13.993,9.443 14.101,9.404 14.235,9.404H16.054C16.188,9.404 16.299,9.443 16.388,9.521C16.483,9.593 16.53,9.696 16.53,9.83C16.53,9.913 16.513,9.991 16.48,10.064C16.447,10.136 16.388,10.225 16.305,10.331L15.144,11.825V11.858H16.213Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val muteAudioOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_mute_audio_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M20.024,20.475L18.753,21.749L13.716,16.724V20.895C13.716,20.936 13.717,21.05 13.705,21.153C13.693,21.249 13.649,21.562 13.366,21.804C13.257,21.898 13.127,21.967 12.988,22.006C12.63,22.107 12.344,21.971 12.258,21.927C12.165,21.88 12.071,21.816 12.037,21.793L5.619,17.515H4.658C4.303,17.515 3.965,17.516 3.685,17.486C3.388,17.453 3.051,17.378 2.741,17.153C2.58,17.035 2.438,16.893 2.321,16.732C2.096,16.421 2.019,16.085 1.987,15.788C1.957,15.508 1.958,15.169 1.958,14.815V9.184C1.958,8.829 1.957,8.49 1.987,8.21C2.02,7.913 2.096,7.577 2.321,7.267C2.438,7.106 2.58,6.964 2.741,6.847L2.859,6.769C3.073,6.64 3.296,6.575 3.506,6.538L1.602,4.639L2.873,3.365L20.024,20.475ZM4.658,8.284C4.264,8.284 4.039,8.286 3.879,8.303C3.837,8.307 3.809,8.313 3.791,8.317C3.787,8.334 3.782,8.363 3.777,8.405C3.76,8.565 3.759,8.789 3.759,9.184V14.815C3.759,15.209 3.76,15.434 3.777,15.595C3.782,15.636 3.787,15.664 3.791,15.682C3.809,15.686 3.837,15.692 3.879,15.696C4.039,15.714 4.263,15.715 4.658,15.715H6.165L6.392,15.866L11.916,19.549V14.928L5.256,8.284H4.658Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M19.144,5.002C22.581,8.44 22.958,13.775 20.28,17.632L18.982,16.333C20.971,13.203 20.602,9.006 17.87,6.275L19.144,5.002Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.207,7.938C18.013,9.744 18.364,12.454 17.261,14.613L15.889,13.24C16.341,11.869 16.024,10.3 14.935,9.21L16.207,7.938Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M12.818,1.974L12.956,1.997L13.061,2.03C13.131,2.055 13.197,2.089 13.259,2.129L13.349,2.194L13.45,2.289C13.66,2.518 13.695,2.78 13.705,2.862C13.718,2.966 13.716,3.083 13.716,3.128V11.067L11.916,9.267V4.568L9.23,6.581L7.945,5.296L11.988,2.264C12.025,2.237 12.117,2.166 12.208,2.114C12.278,2.073 12.509,1.943 12.818,1.974Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val muteFilled20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_mute_filled_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M5.437,11.148C6.087,12.891 7.87,14.173 10.001,14.173C10.487,14.173 10.953,14.103 11.394,13.979L12.425,15.011C11.872,15.236 11.278,15.389 10.657,15.451V16.728H9.345V15.451C6.973,15.212 4.985,13.695 4.206,11.607L3.977,10.992L5.207,10.533L5.437,11.148ZM15.63,14.702L14.64,15.692L4.9,5.953L5.891,4.963L15.63,14.702ZM10.392,12.977C10.263,12.992 10.133,13.002 10.001,13.002C8.139,13.002 6.63,11.493 6.63,9.631V9.216L10.392,12.977ZM16.024,10.992L15.795,11.607C15.636,12.034 15.423,12.434 15.17,12.807L14.22,11.856C14.355,11.631 14.473,11.395 14.565,11.148L14.795,10.533L16.024,10.992ZM10.001,3.272C11.862,3.273 13.371,4.782 13.371,6.644V9.631C13.371,10.042 13.297,10.435 13.162,10.799L7.172,4.809C7.773,3.884 8.816,3.272 10.001,3.272Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val newChatOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_new_chat_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12,2.293C6.639,2.293 2.292,6.64 2.292,12C2.293,13.487 2.628,14.898 3.227,16.16L3.614,16.974L5.24,16.201L4.853,15.388C4.366,14.362 4.093,13.214 4.093,12C4.093,7.634 7.633,4.094 12,4.094C16.366,4.094 19.906,7.634 19.906,12C19.906,16.367 16.366,19.906 12,19.906C11.102,19.906 10.448,19.842 9.871,19.683C9.302,19.526 8.76,19.264 8.111,18.811C7.143,18.136 5.752,17.894 4.602,18.62L4.583,18.632L4.565,18.646L3.548,19.361L4.145,21.142L5.58,20.132C5.972,19.895 6.56,19.923 7.082,20.286C7.858,20.828 8.583,21.195 9.391,21.418C10.19,21.639 11.026,21.706 12,21.706C17.36,21.706 21.705,17.361 21.706,12C21.706,6.639 17.36,2.293 12,2.293ZM11.093,7.955V11.1H7.955V12.899H11.093V16.045H12.893V12.899H16.044V11.1H12.893V7.955H11.093Z").toNodes(),
                fill = SolidColor(Color(0xFF0F1115)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val newChatOutline24White: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_new_chat_outline_24_white",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M1.6,12C1.6,6.256 6.256,1.6 12,1.6C17.744,1.6 22.4,6.256 22.4,12C22.4,17.744 17.744,22.4 12,22.4C10.953,22.4 10.059,22.327 9.206,22.092C8.343,21.854 7.568,21.462 6.735,20.881C6.15,20.473 5.478,20.437 5.024,20.72L3.503,21.792L2.906,20.011L4.021,19.227L4.058,19.201C5.268,18.437 6.738,18.688 7.765,19.404C8.47,19.897 9.061,20.184 9.685,20.356C10.317,20.531 11.029,20.6 12,20.6C16.75,20.6 20.6,16.75 20.6,12C20.6,7.25 16.75,3.4 12,3.4C7.25,3.4 3.4,7.25 3.4,12C3.4,13.321 3.697,14.57 4.228,15.686L4.613,16.498L2.987,17.271L2.602,16.458C1.959,15.106 1.6,13.593 1.6,12Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.093,11.1H7.707V12.9H11.093V16.293H12.894V12.9H16.293V11.1H12.894V7.707H11.093V11.1Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val notification: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_notification",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 28f,
            viewportHeight = 21f,
        ).apply {
            group(
                rotate = 0f, pivotX = 0f, pivotY = 0f,
                scaleX = 0.92f, scaleY = 0.69f,
                translationX = 1.12f, translationY = 3.255f,
            ) {
                addPath(
                    pathData = PathParser().parsePathString("M27.365,1.764C27.074,1.619 26.943,1.901 26.783,2.039C26.725,2.09 26.674,2.147 26.623,2.205C26.223,2.719 25.692,2.966 25.045,2.944C24.049,2.871 23.22,3.212 22.551,3.958C22.434,3.06 21.954,2.444 21.118,2.118C20.682,1.959 20.318,1.706 20.02,1.344C19.853,1.054 19.736,0.75 19.671,0.424C19.605,0.236 19.547,0.04 19.329,0.004C19.118,-0.032 19.009,0.171 18.922,0.337C18.573,1.018 18.413,1.749 18.435,2.524C18.427,3.335 18.609,4.095 18.987,4.812C19.358,5.529 19.882,6.115 20.551,6.557C20.718,6.637 20.769,6.767 20.696,6.941C20.602,7.274 20.493,7.599 20.398,7.925C20.333,8.15 20.231,8.186 20.013,8.099C19.256,7.766 18.595,7.303 18.013,6.702C17.082,5.659 16.086,4.682 15.024,3.762C14.762,3.567 14.508,3.378 14.231,3.205C13.075,2.068 14.384,1.134 14.675,1.025C14.973,0.909 14.784,0.511 13.78,0.518C12.697,0.591 11.671,0.859 10.69,1.329C10.515,1.394 10.34,1.452 10.151,1.489C9.053,1.271 7.948,1.235 6.835,1.373C4.639,1.597 2.915,2.633 1.665,4.486C0.152,6.68 -0.328,9.091 0.217,11.712C0.676,14.442 1.963,16.679 4.079,18.431C6.203,20.314 8.668,21.168 11.49,20.973C13.679,20.973 15.591,20.234 17.235,18.764C17.889,19.061 18.573,19.221 19.285,19.235C19.947,19.286 20.594,19.235 21.242,19.083C22.093,18.902 22.034,18.084 21.722,17.939C19.242,16.766 19.787,17.237 19.293,16.838C20.551,15.296 22.456,13.696 23.198,8.512C23.227,8.179 23.227,7.846 23.198,7.513C23.198,7.303 23.242,7.23 23.467,7.201C24.092,7.129 24.689,6.933 25.241,6.615C26.849,5.71 27.496,4.218 27.649,2.437C27.67,2.184 27.649,1.901 27.365,1.764ZM13.358,17.83C10.959,15.883 9.795,15.238 9.301,15.267C8.806,15.296 8.93,15.825 9.031,16.172C9.126,16.491 9.271,16.781 9.461,17.048C9.562,17.164 9.599,17.295 9.57,17.447C9.541,17.591 9.461,17.7 9.322,17.772C8.551,18.265 7.206,17.606 7.148,17.57C5.519,16.614 4.254,15.303 3.345,13.638C2.435,11.98 1.941,10.206 1.854,8.309C1.854,7.853 1.963,7.686 2.428,7.607C3.01,7.491 3.585,7.476 4.174,7.556C6.639,7.933 8.733,9.026 10.457,10.858C11.41,11.915 12.26,13.066 12.988,14.29C13.78,15.578 14.726,16.737 15.831,17.758C16.158,18.048 16.5,18.315 16.857,18.562C15.955,18.67 14.42,18.692 13.358,17.83ZM14.522,10.184C14.515,9.945 14.638,9.822 14.878,9.815C14.915,9.808 14.958,9.808 14.995,9.815C15.046,9.837 15.089,9.866 15.133,9.902C15.242,10.025 15.264,10.155 15.198,10.3C15.14,10.452 15.031,10.525 14.871,10.532C14.638,10.532 14.529,10.416 14.522,10.184ZM18.1,12.081C17.882,12.19 17.656,12.248 17.424,12.27C17.082,12.284 16.769,12.183 16.5,11.973C16.166,11.763 15.955,11.459 15.868,11.068C15.831,10.836 15.831,10.604 15.868,10.373C15.976,10.018 15.882,9.728 15.591,9.504C15.358,9.33 15.097,9.25 14.799,9.272C14.697,9.265 14.602,9.229 14.508,9.178C14.311,9.084 14.267,8.939 14.377,8.751C14.442,8.657 14.515,8.577 14.595,8.497C15.038,8.28 15.482,8.28 15.918,8.497C16.362,8.736 16.755,9.048 17.082,9.431C17.373,9.772 17.635,10.134 17.867,10.517C18.078,10.829 18.253,11.169 18.376,11.531C18.456,11.77 18.369,11.965 18.1,12.081Z").toNodes(),
                    fill = SolidColor(Color(0xFF426EFE)),
                    pathFillType = PathFillType.NonZero,
                )
            }
        }.build()
    }
    val paperclipOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_paperclip_outline_20",
            defaultWidth = 22.dp,
            defaultHeight = 22.dp,
            viewportWidth = 22f,
            viewportHeight = 22f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M7.536,10.966L11.722,6.78L13.045,8.102L8.859,12.288C8.455,12.692 8.455,13.346 8.859,13.75C9.262,14.153 9.917,14.153 10.32,13.75L15.155,8.915C16.327,7.743 16.327,5.841 15.155,4.669C13.983,3.497 12.081,3.497 10.909,4.669L5.855,9.723C3.913,11.665 3.914,14.812 5.855,16.753C7.797,18.694 10.943,18.694 12.885,16.753L18.149,11.489L19.471,12.812L14.207,18.076C11.536,20.747 7.204,20.747 4.533,18.076C1.861,15.404 1.861,11.073 4.533,8.402L9.587,3.347C11.49,1.445 14.574,1.445 16.477,3.347C18.379,5.25 18.38,8.335 16.477,10.238L11.643,15.072C10.509,16.206 8.67,16.206 7.536,15.072C6.402,13.938 6.402,12.1 7.536,10.966Z").toNodes(),
                fill = SolidColor(Color(0xFFF8F8F8)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val phoneFill: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_phone_fill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M8.307,15.977C7.455,15.126 6.687,14.231 6.004,13.292C5.326,12.348 4.787,11.409 4.387,10.476C3.993,9.538 3.797,8.646 3.797,7.8C3.797,7.232 3.895,6.699 4.092,6.201C4.295,5.703 4.61,5.251 5.039,4.845C5.566,4.341 6.134,4.089 6.742,4.089C6.98,4.089 7.209,4.138 7.429,4.237C7.655,4.335 7.846,4.497 8.003,4.723L9.88,7.365C10.036,7.574 10.149,7.768 10.219,7.948C10.288,8.127 10.323,8.295 10.323,8.452C10.323,8.654 10.268,8.851 10.158,9.042C10.048,9.228 9.894,9.422 9.697,9.625L9.071,10.259C8.979,10.346 8.932,10.456 8.932,10.589C8.932,10.653 8.944,10.717 8.967,10.781C8.99,10.844 9.014,10.899 9.037,10.946C9.182,11.218 9.436,11.568 9.801,11.997C10.166,12.426 10.563,12.858 10.992,13.292C11.427,13.726 11.858,14.126 12.287,14.491C12.716,14.85 13.069,15.105 13.347,15.256C13.388,15.279 13.44,15.302 13.503,15.325C13.567,15.349 13.634,15.36 13.703,15.36C13.837,15.36 13.95,15.314 14.042,15.221L14.668,14.595C14.865,14.399 15.056,14.245 15.241,14.135C15.433,14.025 15.63,13.97 15.832,13.97C15.995,13.97 16.163,14.005 16.337,14.074C16.516,14.144 16.71,14.251 16.919,14.396L19.595,16.299C19.81,16.455 19.963,16.641 20.056,16.855C20.154,17.063 20.204,17.284 20.204,17.515C20.204,17.817 20.137,18.112 20.004,18.402C19.876,18.697 19.694,18.975 19.456,19.236C19.056,19.682 18.608,20.007 18.109,20.209C17.617,20.418 17.087,20.522 16.519,20.522C15.673,20.522 14.778,20.319 13.834,19.914C12.895,19.514 11.951,18.97 11.001,18.28C10.056,17.597 9.158,16.829 8.307,15.977Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val phoneOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_phone_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12.528,6.268C12.387,5.532 12.024,4.683 11.534,3.97C11.058,3.278 10.356,2.56 9.464,2.322C7.657,1.841 5.604,2.709 5.007,4.64C3.973,7.987 4.079,11.355 6.44,15.444C8.8,19.532 11.717,21.403 15.161,22.221C17.135,22.689 18.916,21.339 19.402,19.536C19.643,18.644 19.371,17.677 19.01,16.919C18.637,16.137 18.083,15.399 17.517,14.909C17.397,14.805 17.277,14.697 17.16,14.587C16.975,14.414 16.829,14.277 16.72,14.193C16.014,13.651 15.212,13.51 14.363,13.778C14.234,13.819 14.084,13.884 13.904,13.961L13.336,14.203C12.658,14.492 12.533,14.528 12.401,14.528C12.319,14.528 12.152,14.494 12.076,14.463C11.943,14.407 11.887,14.364 11.523,13.991C11.268,13.728 11.02,13.414 10.777,12.993L10.775,12.989C10.531,12.567 10.383,12.195 10.283,11.843C10.15,11.372 10.141,11.306 10.156,11.169C10.166,11.074 10.232,10.888 10.283,10.807C10.35,10.701 10.443,10.618 11.011,10.211L11.488,9.869C11.643,9.758 11.773,9.667 11.872,9.581C12.565,8.98 12.863,8.191 12.741,7.282C12.723,7.151 12.678,6.959 12.622,6.716C12.588,6.567 12.556,6.416 12.528,6.268ZM16.374,16.229C16.739,16.544 17.153,17.081 17.434,17.67C17.727,18.283 17.797,18.784 17.717,19.082C17.433,20.133 16.467,20.737 15.564,20.523C12.585,19.816 10.071,18.241 7.952,14.571C5.839,10.911 5.781,8.048 6.675,5.156C6.951,4.261 7.963,3.728 9.014,4.008C9.312,4.087 9.71,4.398 10.095,4.958C10.465,5.496 10.722,6.124 10.813,6.597C10.846,6.768 10.882,6.939 10.921,7.108C10.953,7.245 10.975,7.341 10.991,7.412C10.998,7.448 11.004,7.474 11.007,7.492C11.011,7.509 11.012,7.515 11.011,7.514C11.055,7.841 10.977,8.046 10.728,8.262C10.707,8.281 10.663,8.314 10.472,8.451L9.994,8.792C9.526,9.127 9.099,9.416 8.81,9.87C8.615,10.177 8.46,10.621 8.421,10.982C8.364,11.506 8.482,11.89 8.603,12.317C8.741,12.807 8.946,13.313 9.263,13.862L9.265,13.865C9.582,14.413 9.917,14.843 10.271,15.207C10.6,15.545 10.89,15.858 11.403,16.073C11.691,16.194 12.088,16.274 12.4,16.274C12.968,16.274 13.461,16.047 14.02,15.809L14.588,15.567C14.81,15.472 14.862,15.452 14.89,15.443C15.195,15.347 15.403,15.383 15.657,15.578C15.655,15.577 15.66,15.581 15.675,15.593C15.689,15.606 15.708,15.623 15.735,15.647C15.79,15.697 15.863,15.765 15.968,15.863C16.1,15.986 16.236,16.11 16.374,16.229Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val phoneRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_phone_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M15.857,20.008C15.011,20.008 14.143,19.859 13.253,19.56C12.363,19.262 11.46,18.809 10.543,18.201C9.626,17.599 8.709,16.845 7.792,15.939C6.87,15.033 6.102,14.119 5.489,13.196C4.882,12.274 4.426,11.371 4.122,10.486C3.818,9.597 3.667,8.742 3.667,7.923C3.667,7.163 3.78,6.52 4.008,5.994C4.242,5.468 4.605,4.974 5.099,4.513C5.126,4.491 5.156,4.467 5.188,4.44C5.221,4.407 5.253,4.377 5.286,4.35C5.823,3.889 6.363,3.661 6.905,3.667C7.22,3.672 7.521,3.759 7.809,3.927C8.102,4.09 8.368,4.345 8.606,4.692L10.38,7.215C10.657,7.606 10.793,8.012 10.787,8.436C10.787,8.853 10.614,9.233 10.266,9.575L9.681,10.169C9.567,10.283 9.504,10.4 9.493,10.519C9.488,10.633 9.534,10.76 9.632,10.901C9.74,11.059 9.873,11.232 10.03,11.422C10.188,11.612 10.353,11.805 10.527,12C10.706,12.195 10.888,12.383 11.072,12.562C11.257,12.746 11.457,12.939 11.674,13.139C11.897,13.34 12.114,13.527 12.325,13.701C12.537,13.875 12.716,14.013 12.863,14.116C13.003,14.224 13.131,14.276 13.245,14.271C13.359,14.26 13.473,14.195 13.587,14.075L14.181,13.481C14.528,13.145 14.91,12.977 15.328,12.977C15.746,12.977 16.153,13.112 16.549,13.384L19.007,15.092C19.376,15.342 19.644,15.608 19.812,15.89C19.98,16.172 20.065,16.468 20.065,16.777C20.065,17.043 20.002,17.317 19.877,17.599C19.758,17.881 19.582,18.152 19.348,18.413C19.321,18.445 19.292,18.478 19.259,18.51C19.232,18.549 19.202,18.581 19.169,18.608C18.719,19.096 18.234,19.452 17.713,19.674C17.192,19.897 16.573,20.008 15.857,20.008ZM15.841,18.576C16.275,18.57 16.677,18.492 17.045,18.34C17.42,18.188 17.734,17.96 17.989,17.656C18.022,17.624 18.049,17.594 18.071,17.566C18.098,17.534 18.122,17.501 18.144,17.469C18.334,17.246 18.429,17.021 18.429,16.793C18.429,16.679 18.402,16.571 18.347,16.468C18.299,16.359 18.212,16.264 18.087,16.183L15.914,14.743C15.757,14.645 15.597,14.599 15.434,14.604C15.277,14.61 15.127,14.688 14.986,14.84L14.384,15.451C14.07,15.776 13.744,15.942 13.408,15.947C13.071,15.947 12.765,15.849 12.488,15.654C12.298,15.524 12.068,15.345 11.796,15.117C11.525,14.884 11.24,14.631 10.942,14.36C10.644,14.083 10.361,13.812 10.095,13.546C9.727,13.188 9.355,12.798 8.981,12.374C8.606,11.946 8.313,11.58 8.102,11.276C7.906,10.994 7.809,10.684 7.809,10.348C7.814,10.012 7.98,9.686 8.305,9.372L8.924,8.769C9.07,8.628 9.146,8.479 9.152,8.322C9.162,8.159 9.116,8.002 9.013,7.85L7.508,5.612C7.361,5.384 7.16,5.27 6.905,5.27C6.797,5.27 6.683,5.294 6.564,5.343C6.45,5.392 6.338,5.463 6.23,5.555C6.197,5.576 6.165,5.601 6.132,5.628C6.105,5.65 6.075,5.677 6.043,5.709C5.739,5.97 5.511,6.284 5.359,6.653C5.207,7.017 5.129,7.416 5.123,7.85C5.112,8.539 5.256,9.279 5.555,10.071C5.858,10.858 6.292,11.664 6.857,12.488C7.421,13.308 8.096,14.105 8.883,14.881C9.664,15.657 10.459,16.321 11.267,16.875C12.076,17.428 12.868,17.851 13.644,18.144C14.42,18.443 15.152,18.587 15.841,18.576Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val phoneRotateVerticalOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_phone_rotate_vertical_outline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M8.4,1.909C9.652,1.909 10.668,1.908 11.478,1.995C12.305,2.084 13.035,2.274 13.668,2.734C14.035,3 14.357,3.323 14.623,3.689C15.083,4.322 15.274,5.046 15.363,5.868C15.45,6.672 15.449,7.678 15.449,8.917V15.083C15.449,16.321 15.45,17.329 15.363,18.133C15.274,18.954 15.083,19.679 14.623,20.311C14.357,20.678 14.035,21 13.668,21.266C13.035,21.727 12.305,21.917 11.478,22.006C10.668,22.093 9.652,22.091 8.4,22.091C7.148,22.091 6.132,22.093 5.322,22.006C4.495,21.917 3.765,21.727 3.131,21.266C2.765,21 2.443,20.678 2.176,20.311C1.717,19.679 1.526,18.954 1.437,18.133C1.35,17.329 1.351,16.321 1.351,15.083V8.917C1.351,7.678 1.35,6.672 1.437,5.868C1.526,5.046 1.717,4.322 2.176,3.689C2.443,3.323 2.765,3 3.131,2.734C3.765,2.274 4.495,2.084 5.322,1.995C6.132,1.908 7.148,1.909 8.4,1.909ZM8.4,3.829C7.105,3.829 6.211,3.83 5.527,3.903C4.86,3.975 4.509,4.106 4.259,4.287C4.056,4.435 3.877,4.614 3.729,4.817C3.547,5.068 3.417,5.416 3.345,6.075C3.272,6.752 3.271,7.635 3.271,8.917V15.083C3.271,16.364 3.272,17.248 3.345,17.925C3.417,18.583 3.547,18.932 3.729,19.182C3.877,19.386 4.056,19.566 4.259,19.714C4.509,19.895 4.86,20.025 5.527,20.096C6.211,20.17 7.105,20.172 8.4,20.172C9.694,20.172 10.589,20.17 11.273,20.096C11.94,20.025 12.291,19.895 12.54,19.714C12.744,19.566 12.923,19.386 13.071,19.182C13.253,18.932 13.383,18.583 13.455,17.925C13.528,17.248 13.529,16.364 13.529,15.083V8.917C13.529,7.635 13.528,6.752 13.455,6.075C13.383,5.416 13.253,5.068 13.071,4.817C12.923,4.614 12.744,4.435 12.54,4.287C12.291,4.106 11.94,3.975 11.273,3.903C10.589,3.83 9.694,3.829 8.4,3.829ZM11.107,18.83H5.694V16.91H11.107V18.83Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M19.162,19.543C18.485,19.616 17.601,19.618 16.32,19.618L16.32,21.538L16.385,21.538C17.594,21.538 18.579,21.538 19.369,21.452C20.191,21.363 20.915,21.172 21.548,20.713C21.914,20.446 22.237,20.124 22.503,19.757C22.963,19.124 23.153,18.395 23.242,17.567C23.328,16.77 23.328,15.774 23.328,14.549L23.328,14.489L23.328,14.428C23.328,13.204 23.328,12.208 23.242,11.41C23.153,10.583 22.963,9.854 22.503,9.22C22.237,8.854 21.914,8.531 21.548,8.265C20.915,7.805 20.191,7.615 19.369,7.525C18.579,7.44 17.594,7.44 16.385,7.44L16.32,7.44L16.32,9.359C17.601,9.359 18.485,9.361 19.162,9.434C19.82,9.506 20.169,9.636 20.419,9.818C20.623,9.966 20.802,10.145 20.95,10.349C21.131,10.598 21.262,10.948 21.333,11.615C21.407,12.299 21.408,13.194 21.408,14.489C21.408,15.783 21.407,16.678 21.333,17.362C21.262,18.029 21.131,18.38 20.95,18.629C20.802,18.833 20.623,19.012 20.419,19.16C20.169,19.342 19.82,19.472 19.162,19.543Z").toNodes(),
                fill = SolidColor(Color(0x4A0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val photoEmptyOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_photo_empty_outline",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 29f,
            viewportHeight = 29f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M4.117,20.101C4.118,21.973 5.627,23.483 7.499,23.483H15.66V25.117H7.499C4.725,25.117 2.484,22.875 2.483,20.101V19.437H4.117V20.101ZM26.516,20.101C26.515,22.875 24.274,25.117 21.499,25.117H20.101C19.884,25.117 19.676,25.031 19.522,24.878L18.473,23.828L19.628,22.673L20.438,23.484H21.499C23.372,23.484 24.883,21.973 24.883,20.101H26.516ZM17.935,20.981L17.358,21.558L16.781,22.135L13.898,19.253L15.053,18.098L17.935,20.981ZM26.517,18.584H24.883V12.751H26.517V18.584ZM4.117,15.672C4.324,15.519 4.529,15.363 4.737,15.212L5.218,15.872L5.698,16.533C5.185,16.906 4.675,17.285 4.167,17.668C3.477,18.188 2.484,17.699 2.483,16.829V10.612H4.117V15.672ZM8.158,13.644C8.519,13.62 8.874,13.632 9.225,13.682L9.575,13.744L9.581,13.745C10.234,13.889 10.709,14.037 11.283,14.457C11.79,14.828 12.384,15.422 13.34,16.371L12.189,17.53C11.185,16.534 10.7,16.055 10.318,15.775C10.004,15.545 9.778,15.461 9.229,15.34C8.913,15.273 8.599,15.251 8.271,15.273H8.269C7.913,15.297 7.563,15.4 7.118,15.631C6.994,15.695 6.861,15.771 6.72,15.856L5.872,14.461C6.038,14.36 6.202,14.267 6.363,14.183C6.957,13.874 7.527,13.687 8.158,13.644ZM16.822,11.067C16.754,11.266 16.717,11.479 16.717,11.7C16.717,12.789 17.611,13.682 18.7,13.683C18.921,13.683 19.132,13.644 19.331,13.576L20.553,14.798C20.01,15.124 19.377,15.316 18.7,15.316C16.709,15.316 15.083,13.691 15.083,11.7C15.083,11.023 15.275,10.39 15.602,9.847L16.822,11.067ZM18.7,8.083C20.691,8.083 22.316,9.71 22.316,11.7C22.316,12.381 22.121,13.017 21.791,13.561L20.572,12.343C20.642,12.141 20.683,11.925 20.683,11.7C20.682,10.612 19.789,9.717 18.7,9.717C18.475,9.717 18.259,9.757 18.057,9.827L16.836,8.606C17.381,8.276 18.019,8.083 18.7,8.083ZM21.499,3.883C24.274,3.883 26.515,6.125 26.516,8.899V11.001H24.883V8.899C24.882,7.027 23.372,5.517 21.499,5.517H11.34V3.883H21.499ZM9.249,5.517H7.499C5.627,5.517 4.118,7.027 4.117,8.899H2.483C2.484,6.125 4.725,3.883 7.499,3.883H9.249V5.517Z").toNodes(),
                fill = SolidColor(Color(0xFFA2A4A6)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val photoErrorFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_photo_error_filled",
            defaultWidth = 41.dp,
            defaultHeight = 40.dp,
            viewportWidth = 41f,
            viewportHeight = 40f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M29.792,5.01C31.741,5.04 32.927,5.164 33.889,5.654C35.018,6.23 35.936,7.147 36.511,8.276C37.165,9.56 37.166,11.24 37.166,14.6V25.4L37.156,27.625C37.125,29.575 37.002,30.761 36.511,31.724L36.281,32.137C35.704,33.077 34.877,33.842 33.889,34.346L33.644,34.461C32.724,34.856 31.579,34.962 29.792,34.99L27.566,35H13.432L12.108,34.994L19.376,22.403L32.482,31.315L32.56,31.272C32.978,31.016 33.318,30.649 33.542,30.21L33.586,30.104C33.64,29.956 33.714,29.665 33.762,29.069C33.83,28.236 33.833,27.135 33.833,25.4V14.6C33.833,12.865 33.83,11.764 33.762,10.931C33.714,10.335 33.64,10.043 33.586,9.896L33.542,9.79C33.318,9.351 32.978,8.984 32.56,8.728L32.375,8.625C32.282,8.577 32.028,8.468 31.235,8.403C30.402,8.335 29.301,8.333 27.566,8.333H27.501L29.42,5.008L29.792,5.01ZM22.209,8.333H13.432C11.698,8.333 10.596,8.335 9.763,8.403C9.168,8.452 8.876,8.526 8.728,8.579L8.623,8.625C8.184,8.849 7.816,9.189 7.56,9.606L7.458,9.79C7.41,9.884 7.301,10.137 7.236,10.931C7.202,11.347 7.184,11.831 7.175,12.427L7.166,14.6V21.339L10.253,18.968C11.394,18.092 12.969,18.047 14.159,18.855L15.577,19.819L7.173,34.376L7.109,34.346C6.121,33.842 5.295,33.077 4.718,32.137L4.487,31.724C3.996,30.761 3.873,29.575 3.842,27.625L3.833,25.4V14.6C3.833,11.45 3.833,9.777 4.372,8.522L4.487,8.276C4.99,7.288 5.756,6.462 6.696,5.886L7.109,5.654C8.071,5.164 9.257,5.04 11.208,5.01L13.432,5H24.134L22.209,8.333ZM27.03,12C28.703,12 30.06,13.357 30.06,15.03C30.06,16.704 28.703,18.06 27.03,18.06C25.356,18.06 24,16.704 24,15.03C24,13.357 25.356,12 27.03,12Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val photoErrorFilled2: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_photo_error_filled_2",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 29f,
            viewportHeight = 29f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M21.004,4.007C22.369,4.028 23.2,4.115 23.873,4.458C24.663,4.861 25.306,5.503 25.708,6.293C26.166,7.191 26.166,8.368 26.166,10.72V18.28L26.159,19.838C26.138,21.203 26.051,22.032 25.708,22.706L25.547,22.996C25.144,23.654 24.565,24.19 23.873,24.542L23.701,24.623C23.058,24.899 22.255,24.973 21.004,24.993L19.447,25H9.553L8.626,24.995L13.714,16.183L22.888,22.421L22.943,22.391C23.235,22.211 23.473,21.954 23.629,21.646L23.661,21.573C23.699,21.47 23.75,21.265 23.784,20.849C23.832,20.265 23.833,19.494 23.833,18.28V10.72C23.833,9.505 23.832,8.735 23.784,8.151C23.75,7.735 23.699,7.53 23.661,7.427L23.629,7.353C23.473,7.046 23.235,6.789 22.943,6.609L22.814,6.537C22.748,6.504 22.57,6.427 22.015,6.382C21.432,6.334 20.66,6.333 19.447,6.333H19.402L20.744,4.005L21.004,4.007ZM15.696,6.333H9.553C8.339,6.333 7.568,6.334 6.985,6.382C6.569,6.416 6.364,6.468 6.26,6.505L6.186,6.537C5.879,6.694 5.622,6.932 5.443,7.225L5.37,7.353C5.337,7.418 5.26,7.596 5.215,8.151C5.191,8.443 5.18,8.781 5.173,9.198L5.166,10.72V15.438L7.328,13.777C8.127,13.164 9.229,13.132 10.062,13.698L11.054,14.373L5.171,24.563L5.126,24.542C4.435,24.19 3.857,23.654 3.453,22.996L3.291,22.706C2.948,22.032 2.862,21.203 2.84,19.838L2.833,18.28V10.72C2.833,8.515 2.833,7.344 3.21,6.466L3.291,6.293C3.644,5.602 4.18,5.024 4.837,4.62L5.126,4.458C5.8,4.115 6.631,4.028 7.995,4.007L9.553,4H17.044L15.696,6.333ZM19.071,8.9C20.242,8.9 21.192,9.85 21.192,11.021C21.192,12.193 20.242,13.143 19.071,13.143C17.899,13.142 16.949,12.193 16.949,11.021C16.949,9.85 17.899,8.901 19.071,8.9Z").toNodes(),
                fill = SolidColor(Color(0xFFA2A4A6)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val photoFilled16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_photo_filled_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10.284,4.761C11.232,4.761 12.001,5.529 12.001,6.478C12.001,7.426 11.232,8.195 10.284,8.195C9.336,8.195 8.566,7.426 8.566,6.478C8.566,5.529 9.336,4.761 10.284,4.761Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.762,1.921C9.836,1.921 10.701,1.92 11.392,1.992C12.095,2.066 12.704,2.224 13.237,2.597C13.598,2.849 13.913,3.162 14.165,3.523C14.538,4.057 14.694,4.666 14.769,5.37C14.841,6.061 14.841,6.925 14.841,8C14.841,9.074 14.841,9.939 14.769,10.63C14.694,11.333 14.537,11.942 14.165,12.476C13.913,12.837 13.598,13.151 13.237,13.403C12.704,13.776 12.095,13.933 11.392,14.007C10.701,14.08 9.836,14.078 8.762,14.078H7.239C6.165,14.078 5.3,14.079 4.609,14.007C3.906,13.933 3.296,13.776 2.763,13.403C2.402,13.151 2.088,12.837 1.836,12.476C1.463,11.942 1.306,11.333 1.231,10.63C1.159,9.939 1.16,9.075 1.16,8C1.16,6.925 1.159,6.061 1.231,5.37C1.306,4.666 1.463,4.057 1.836,3.523C2.088,3.163 2.402,2.849 2.763,2.597C3.296,2.224 3.906,2.066 4.609,1.992C5.3,1.92 6.165,1.921 7.239,1.921H8.762ZM7.239,3.421C6.133,3.421 5.36,3.422 4.767,3.484C4.186,3.545 3.862,3.659 3.622,3.826C3.406,3.977 3.217,4.166 3.065,4.383C2.898,4.623 2.785,4.947 2.724,5.527C2.661,6.121 2.66,6.893 2.66,8C2.66,8.04 2.66,8.079 2.66,8.118C2.965,7.921 3.244,7.774 3.535,7.68C4.447,7.385 5.438,7.455 6.299,7.876C6.879,8.16 7.377,8.671 8.142,9.436L11.221,12.515C11.225,12.514 11.23,12.515 11.234,12.515C11.815,12.454 12.139,12.341 12.379,12.173C12.595,12.022 12.783,11.833 12.935,11.617C13.102,11.377 13.216,11.053 13.277,10.473C13.34,9.879 13.341,9.107 13.341,8C13.341,6.893 13.34,6.121 13.277,5.527C13.216,4.947 13.102,4.623 12.935,4.383C12.783,4.166 12.595,3.977 12.379,3.826C12.139,3.658 11.815,3.545 11.234,3.484C10.641,3.422 9.869,3.421 8.762,3.421H7.239Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val photoFilled20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_photo_filled_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12.6,2.15C13.706,2.15 14.604,2.149 15.321,2.227C16.055,2.306 16.699,2.476 17.263,2.886C17.59,3.123 17.877,3.41 18.114,3.737C18.524,4.301 18.694,4.945 18.773,5.679C18.851,6.396 18.85,7.294 18.85,8.4V11.6C18.85,12.706 18.851,13.604 18.773,14.321C18.694,15.055 18.524,15.699 18.114,16.263C17.877,16.59 17.59,16.877 17.263,17.114C16.699,17.524 16.055,17.694 15.321,17.773C14.604,17.851 13.706,17.85 12.6,17.85H7.4C6.294,17.85 5.396,17.851 4.679,17.773C3.945,17.694 3.301,17.524 2.737,17.114C2.41,16.877 2.123,16.59 1.886,16.263C1.476,15.699 1.306,15.055 1.227,14.321C1.149,13.604 1.15,12.706 1.15,11.6V8.4C1.15,7.294 1.149,6.396 1.227,5.679C1.306,4.945 1.476,4.301 1.886,3.737C2.123,3.41 2.41,3.123 2.737,2.886C3.301,2.476 3.945,2.306 4.679,2.227C5.396,2.149 6.294,2.15 7.4,2.15H12.6ZM7.4,3.85C6.257,3.85 5.467,3.852 4.862,3.917C4.274,3.981 3.962,4.097 3.736,4.261C3.554,4.393 3.393,4.554 3.261,4.736C3.097,4.962 2.981,5.274 2.917,5.862C2.852,6.467 2.85,7.257 2.85,8.4V11.356C2.959,11.262 3.099,11.153 3.306,10.994L3.556,10.801C4.705,9.915 5.28,9.472 5.906,9.323C6.463,9.191 7.046,9.219 7.587,9.407C8.195,9.619 8.722,10.117 9.776,11.114L15.038,16.09C15.072,16.087 15.105,16.087 15.138,16.083C15.726,16.019 16.038,15.903 16.264,15.739C16.446,15.607 16.607,15.446 16.739,15.264C16.903,15.038 17.019,14.726 17.083,14.138C17.149,13.533 17.15,12.743 17.15,11.6V8.4C17.15,7.257 17.149,6.467 17.083,5.862C17.019,5.274 16.903,4.962 16.739,4.736C16.607,4.554 16.446,4.393 16.264,4.261C16.038,4.097 15.726,3.981 15.138,3.917C14.533,3.852 13.743,3.85 12.6,3.85H7.4ZM13.021,6C14.027,6 14.843,6.815 14.843,7.821C14.843,8.827 14.027,9.642 13.021,9.643C12.016,9.643 11.2,8.827 11.2,7.821C11.2,6.815 12.016,6 13.021,6Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val photoOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_photo_outline_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10.284,4.255C11.511,4.255 12.507,5.25 12.507,6.478C12.507,7.705 11.511,8.7 10.284,8.7C9.057,8.7 8.062,7.705 8.062,6.478C8.062,5.25 9.057,4.255 10.284,4.255ZM10.284,5.655C9.83,5.655 9.462,6.023 9.462,6.478C9.462,6.932 9.83,7.3 10.284,7.3C10.738,7.3 11.106,6.932 11.106,6.478C11.106,6.023 10.738,5.656 10.284,5.655Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.762,1.971C9.837,1.971 10.699,1.97 11.386,2.042C12.085,2.116 12.685,2.272 13.209,2.638C13.565,2.886 13.875,3.196 14.124,3.552C14.49,4.076 14.645,4.675 14.719,5.375C14.791,6.062 14.79,6.924 14.79,8C14.79,9.075 14.791,9.937 14.719,10.624C14.645,11.324 14.49,11.924 14.124,12.447C13.875,12.803 13.565,13.113 13.209,13.362C12.685,13.728 12.085,13.883 11.386,13.957C10.699,14.029 9.837,14.028 8.762,14.028H7.239C6.164,14.028 5.302,14.029 4.614,13.957C3.915,13.883 3.315,13.728 2.791,13.362C2.435,13.113 2.126,12.803 1.877,12.447C1.511,11.923 1.355,11.324 1.281,10.624C1.209,9.937 1.21,9.075 1.21,8C1.21,6.924 1.209,6.062 1.281,5.375C1.355,4.675 1.511,4.076 1.877,3.552C2.126,3.196 2.435,2.886 2.791,2.638C3.315,2.272 3.915,2.116 4.614,2.042C5.302,1.97 6.164,1.971 7.239,1.971H8.762ZM5.684,9.134C5.148,8.872 4.533,8.828 3.966,9.012C3.666,9.109 3.362,9.314 2.632,9.857C2.641,10.085 2.654,10.29 2.674,10.477C2.735,11.062 2.85,11.396 3.024,11.646C3.179,11.867 3.371,12.059 3.593,12.214C3.843,12.389 4.176,12.503 4.761,12.564C5.358,12.627 6.133,12.629 7.239,12.629H8.762C8.969,12.629 9.165,12.625 9.351,12.625L7.151,10.426C6.309,9.584 6.002,9.29 5.684,9.134ZM7.239,3.371C6.133,3.371 5.358,3.372 4.761,3.435C4.176,3.496 3.843,3.611 3.593,3.785C3.372,3.94 3.179,4.132 3.024,4.354C2.85,4.603 2.735,4.937 2.674,5.521C2.611,6.119 2.61,6.894 2.61,8C2.61,8.05 2.61,8.1 2.61,8.148C2.935,7.935 3.229,7.779 3.535,7.68C4.447,7.385 5.438,7.455 6.299,7.876C6.879,8.16 7.377,8.671 8.142,9.436L11.267,12.561C11.834,12.498 12.161,12.386 12.407,12.214C12.628,12.059 12.821,11.867 12.976,11.646C13.15,11.396 13.265,11.062 13.326,10.477C13.389,9.881 13.391,9.106 13.391,8C13.391,6.894 13.389,6.119 13.326,5.521C13.265,4.937 13.15,4.603 12.976,4.354C12.821,4.132 12.628,3.94 12.407,3.785C12.157,3.611 11.824,3.496 11.239,3.435C10.642,3.372 9.867,3.371 8.762,3.371H7.239Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val photoOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_photo_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13,5.15C14.574,5.15 15.85,6.426 15.85,8C15.85,9.574 14.574,10.85 13,10.85C11.426,10.85 10.15,9.574 10.15,8C10.15,6.426 11.426,5.15 13,5.15ZM13,6.85C12.365,6.85 11.85,7.365 11.85,8C11.85,8.635 12.365,9.15 13,9.15C13.635,9.15 14.15,8.635 14.15,8C14.15,7.365 13.635,6.85 13,6.85Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M12.6,2.15C13.706,2.15 14.604,2.149 15.321,2.227C16.055,2.306 16.699,2.476 17.263,2.886C17.59,3.123 17.877,3.41 18.114,3.737C18.524,4.301 18.694,4.945 18.773,5.679C18.851,6.396 18.85,7.294 18.85,8.4V11.6C18.85,12.706 18.851,13.604 18.773,14.321C18.694,15.055 18.524,15.699 18.114,16.263C17.877,16.59 17.59,16.877 17.263,17.114C16.699,17.524 16.055,17.694 15.321,17.773C14.604,17.851 13.706,17.85 12.6,17.85H7.4C6.294,17.85 5.396,17.851 4.679,17.773C3.945,17.694 3.301,17.524 2.737,17.114C2.41,16.877 2.123,16.59 1.886,16.263C1.476,15.699 1.306,15.055 1.227,14.321C1.149,13.604 1.15,12.706 1.15,11.6V8.4C1.15,7.294 1.149,6.396 1.227,5.679C1.306,4.945 1.476,4.301 1.886,3.737C2.123,3.41 2.41,3.123 2.737,2.886C3.301,2.476 3.945,2.306 4.679,2.227C5.396,2.149 6.294,2.15 7.4,2.15H12.6ZM5.563,10.816C5.31,10.835 5.025,10.929 4.58,11.197C4.138,11.464 3.61,11.856 2.854,12.423C2.858,13.141 2.869,13.69 2.917,14.138C2.981,14.726 3.097,15.038 3.261,15.264C3.393,15.446 3.554,15.607 3.736,15.739C3.962,15.903 4.274,16.019 4.862,16.083C5.467,16.149 6.257,16.15 7.4,16.15H11.949L8.711,12.913C8,12.203 7.511,11.715 7.096,11.376C6.693,11.047 6.424,10.914 6.175,10.86C5.974,10.817 5.768,10.802 5.563,10.816ZM7.4,3.85C6.257,3.85 5.467,3.852 4.862,3.917C4.274,3.981 3.962,4.097 3.736,4.261C3.554,4.393 3.393,4.554 3.261,4.736C3.097,4.962 2.981,5.274 2.917,5.862C2.852,6.467 2.85,7.257 2.85,8.4V10.311C3.157,10.09 3.439,9.899 3.702,9.74C4.266,9.4 4.816,9.166 5.441,9.121C5.808,9.095 6.176,9.121 6.535,9.199C7.148,9.332 7.659,9.642 8.17,10.059C8.668,10.465 9.227,11.025 9.913,11.711L14.333,16.132C14.636,16.122 14.901,16.109 15.138,16.083C15.726,16.019 16.038,15.903 16.264,15.739C16.446,15.607 16.607,15.446 16.739,15.264C16.903,15.038 17.019,14.726 17.083,14.138C17.149,13.533 17.15,12.743 17.15,11.6V8.4C17.15,7.257 17.149,6.467 17.083,5.862C17.019,5.274 16.903,4.962 16.739,4.736C16.607,4.554 16.446,4.393 16.264,4.261C16.038,4.097 15.726,3.981 15.138,3.917C14.533,3.852 13.743,3.85 12.6,3.85H7.4Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val pinOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_pin_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10.46,2.057C10.743,1.971 11.049,2.048 11.258,2.257L17.75,8.749C17.959,8.958 18.036,9.265 17.95,9.547C17.865,9.829 17.631,10.042 17.342,10.1L15.775,10.412L12.52,13.668L12.033,16.593C11.846,17.716 10.467,18.158 9.662,17.353L2.654,10.345C1.849,9.54 2.291,8.161 3.414,7.974L6.339,7.485L9.594,4.232L9.907,2.665L9.937,2.56C10.02,2.32 10.213,2.132 10.46,2.057ZM11.115,4.782C11.084,4.937 11.008,5.08 10.897,5.192L7.29,8.799C7.172,8.917 7.02,8.994 6.856,9.022L4.06,9.487L10.519,15.945L10.984,13.151L11.015,13.03C11.054,12.913 11.12,12.805 11.208,12.717L14.816,9.11L14.904,9.033C14.999,8.963 15.108,8.915 15.224,8.892L15.562,8.823L11.183,4.444L11.115,4.782Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M2.015,17.445C2.065,17.381 2.115,17.317 2.164,17.253C3.113,16.03 4.061,14.807 5.009,13.584C5.479,14.056 5.949,14.529 6.42,15.002C5.192,15.944 3.965,16.886 2.738,17.829C2.673,17.878 2.608,17.927 2.544,17.977C2.367,17.8 2.191,17.623 2.015,17.445Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val playMediaOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_play_media_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M11.405,17.419C11.394,17.506 11.356,17.771 11.117,17.975C11.023,18.055 10.913,18.114 10.795,18.147C10.493,18.232 10.251,18.118 10.173,18.079C10.091,18.037 10.006,17.978 9.968,17.953L4.8,14.507H4.357C3.999,14.507 3.67,14.508 3.4,14.479C3.116,14.448 2.809,14.377 2.529,14.173C2.38,14.065 2.249,13.935 2.141,13.787C1.937,13.506 1.866,13.199 1.835,12.915C1.806,12.644 1.807,12.316 1.807,11.958V8.042C1.807,7.683 1.806,7.355 1.835,7.084C1.866,6.8 1.937,6.494 2.141,6.213C2.249,6.065 2.38,5.934 2.529,5.826C2.809,5.622 3.116,5.551 3.4,5.52C3.67,5.491 3.999,5.492 4.357,5.492H5.404L9.926,2.1C9.965,2.071 10.05,2.006 10.131,1.959C10.204,1.917 10.45,1.78 10.768,1.859C10.89,1.889 11.004,1.946 11.102,2.026C11.356,2.233 11.395,2.512 11.405,2.595C11.416,2.689 11.415,2.795 11.415,2.844V17.179C11.415,17.224 11.416,17.327 11.405,17.419ZM3.307,11.958C3.307,12.349 3.308,12.583 3.327,12.753C3.344,12.91 3.368,12.922 3.355,12.905C3.37,12.926 3.389,12.944 3.411,12.96C3.425,12.965 3.467,12.978 3.561,12.988C3.732,13.006 3.965,13.007 4.357,13.007H5.254L5.444,13.133L9.914,16.114V3.984L6.104,6.842L5.904,6.992H4.357C3.965,6.992 3.732,6.993 3.561,7.011C3.405,7.028 3.393,7.052 3.411,7.04C3.389,7.055 3.37,7.074 3.355,7.095C3.368,7.078 3.344,7.09 3.327,7.246C3.308,7.416 3.307,7.65 3.307,8.042V11.958Z").toNodes(),
                fill = SolidColor(Color(0xFF0F1115)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M13.437,6.681C15.271,8.514 15.271,11.486 13.437,13.319L12.377,12.259C13.624,11.011 13.624,8.989 12.377,7.741L13.437,6.681Z").toNodes(),
                fill = SolidColor(Color(0xFF0F1115)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M15.828,4.29C18.981,7.443 18.981,12.556 15.828,15.709L14.767,14.649C17.335,12.081 17.335,7.918 14.767,5.351L15.828,4.29Z").toNodes(),
                fill = SolidColor(Color(0xFF0F1115)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val playMediaOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_play_media_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12.441,2.426L12.617,2.457L12.771,2.503C12.87,2.54 12.966,2.588 13.056,2.646L13.186,2.741L13.315,2.864C13.588,3.161 13.657,3.527 13.685,3.756C13.717,4.032 13.716,4.38 13.716,4.733V19.397C13.716,19.728 13.717,20.06 13.686,20.325C13.654,20.583 13.571,21.003 13.21,21.311C13.051,21.447 12.865,21.546 12.664,21.603C12.207,21.732 11.811,21.57 11.579,21.453C11.341,21.332 11.066,21.146 10.79,20.962L5.62,17.516H4.658C4.303,17.516 3.965,17.517 3.685,17.487C3.388,17.455 3.051,17.378 2.741,17.153C2.58,17.036 2.438,16.894 2.321,16.733C2.096,16.423 2.02,16.086 1.987,15.79C1.957,15.51 1.958,15.171 1.958,14.816V9.185C1.958,8.83 1.957,8.491 1.987,8.212C2.019,7.915 2.096,7.578 2.321,7.268L2.414,7.151C2.511,7.038 2.62,6.935 2.741,6.847L2.859,6.77C3.136,6.604 3.425,6.542 3.685,6.514C3.965,6.484 4.303,6.485 4.658,6.485H6.361L10.705,3.227C10.987,3.015 11.265,2.805 11.505,2.667C11.705,2.551 12.039,2.386 12.441,2.426ZM11.785,4.668L7.202,8.105L6.962,8.285H4.658C4.263,8.285 4.039,8.286 3.879,8.303C3.837,8.308 3.809,8.314 3.791,8.318C3.787,8.336 3.782,8.364 3.777,8.405C3.76,8.565 3.759,8.79 3.759,9.185V14.816C3.759,15.211 3.76,15.435 3.777,15.595C3.782,15.636 3.787,15.664 3.791,15.682C3.809,15.686 3.837,15.693 3.879,15.698C4.039,15.715 4.263,15.716 4.658,15.716H6.165L6.392,15.867L11.789,19.465C11.833,19.495 11.875,19.521 11.914,19.547C11.914,19.5 11.916,19.45 11.916,19.397V4.733C11.916,4.676 11.914,4.622 11.914,4.571C11.873,4.601 11.831,4.634 11.785,4.668ZM19.144,5.002C23.008,8.866 23.008,15.133 19.144,18.998L17.87,17.724C21.032,14.562 21.032,9.437 17.87,6.275L19.144,5.002ZM16.207,7.938C18.45,10.181 18.45,13.819 16.207,16.062L14.934,14.789C16.474,13.248 16.474,10.752 14.934,9.212L16.207,7.938Z").toNodes(),
                fill = SolidColor(Color(0xFF0F1115)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val playingFill24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_playing_fill_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12.818,1.967L12.956,1.992L13.061,2.024C13.165,2.062 13.262,2.118 13.349,2.188C13.649,2.434 13.694,2.764 13.705,2.856C13.718,2.96 13.716,3.077 13.716,3.123V20.888C13.716,20.929 13.717,21.043 13.705,21.146C13.693,21.242 13.649,21.556 13.366,21.798C13.257,21.892 13.127,21.961 12.988,22.001C12.63,22.102 12.344,21.965 12.258,21.921C12.165,21.875 12.071,21.809 12.037,21.787L5.621,17.509H4.658C4.303,17.509 3.965,17.51 3.685,17.48C3.388,17.448 3.052,17.372 2.741,17.146C2.58,17.029 2.438,16.887 2.321,16.726C2.096,16.416 2.019,16.08 1.987,15.783C1.957,15.503 1.958,15.164 1.958,14.809V9.178C1.958,8.824 1.957,8.485 1.987,8.205C2.02,7.908 2.096,7.572 2.321,7.261L2.414,7.144C2.511,7.031 2.62,6.928 2.741,6.84L2.859,6.763C3.135,6.597 3.425,6.536 3.685,6.507C3.965,6.477 4.303,6.478 4.658,6.478H6.361L11.988,2.258C12.024,2.231 12.117,2.16 12.208,2.107C12.279,2.066 12.51,1.937 12.818,1.967Z").toNodes(),
                fill = SolidColor(Color(0xFFD4D4D4)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M19.144,4.996C23.008,8.86 23.008,15.127 19.144,18.992L17.87,17.718C21.032,14.557 21.032,9.431 17.87,6.269L19.144,4.996Z").toNodes(),
                fill = SolidColor(Color(0xFFD4D4D4)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.207,7.932C18.45,10.175 18.45,13.813 16.207,16.056L14.934,14.783C16.474,13.243 16.474,10.746 14.934,9.206L16.207,7.932Z").toNodes(),
                fill = SolidColor(Color(0xFFD4D4D4)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val plusOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_plus_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10.993,1.147V8.999H18.853V10.999H10.993V18.853H8.993V10.999H1.147V8.999H8.993V1.147H10.993Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val redirectRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_redirect_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M9.63,4.033H5.27C4.564,4.033 3.99,4.605 3.99,5.31L3.99,18.737C3.99,19.442 4.564,20.014 5.27,20.014H18.73C19.436,20.014 20.01,19.442 20.01,18.737V14.341L20.014,14.239C20.065,13.738 20.49,13.348 21.005,13.348C21.52,13.348 21.944,13.738 21.995,14.239L22,14.341V18.737C22,20.539 20.536,22 18.73,22H5.27C3.464,22 2,20.539 2,18.737L2,5.31C2,3.508 3.464,2.047 5.27,2.047L9.63,2.047C10.18,2.047 10.625,2.492 10.625,3.04C10.625,3.588 10.18,4.033 9.63,4.033Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M22,9.612C22,10.161 21.555,10.605 21.005,10.605C20.455,10.605 19.902,10.161 19.902,9.612L20,4L14.37,4.167C13.82,4.167 13.375,3.541 13.375,2.993C13.375,2.444 13.82,2 14.37,2H20.531C21.342,2 22,2.656 22,3.466V9.612Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M19.902,2.695C20.293,2.377 20.87,2.4 21.235,2.764C21.599,3.127 21.622,3.703 21.303,4.093L21.235,4.167L12.5,13C12.111,13.388 11.482,13.388 11.093,13C10.704,12.612 10.704,11.984 11.093,11.596L19.827,2.764L19.902,2.695Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val redoOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_redo_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13.474,4.967L17.454,8.182H9.9C6.539,8.182 3.814,10.907 3.813,14.269C3.814,17.63 6.539,20.355 9.9,20.355H11.814V18.556H9.9C7.533,18.555 5.614,16.636 5.613,14.269C5.613,11.901 7.533,9.983 9.9,9.982H17.467L13.474,13.206L14.542,14.529L18.729,11.148C19.066,10.876 19.377,10.626 19.604,10.394C19.841,10.153 20.08,9.841 20.157,9.415C20.196,9.198 20.196,8.976 20.157,8.759C20.08,8.333 19.841,8.021 19.604,7.779C19.377,7.547 19.066,7.297 18.729,7.024L14.542,3.645L13.474,4.967Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val refreshCircleFill: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_refresh_circle_fill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12,23.5C18.351,23.5 23.5,18.351 23.5,12C23.5,5.649 18.351,0.5 12,0.5C5.649,0.5 0.5,5.649 0.5,12C0.5,18.351 5.649,23.5 12,23.5ZM7.496,15.093C7.032,14.299 6.8,13.42 6.8,12.454C6.8,11.51 7.028,10.657 7.485,9.893C7.949,9.122 8.556,8.512 9.304,8.063C10.061,7.613 10.873,7.389 11.741,7.389C11.794,7.389 11.846,7.393 11.899,7.4C11.959,7.4 12.007,7.404 12.045,7.411L11.236,6.58C11.176,6.513 11.127,6.434 11.09,6.344C11.053,6.247 11.034,6.146 11.034,6.041C11.034,5.839 11.105,5.667 11.247,5.525C11.39,5.375 11.566,5.3 11.775,5.3C11.873,5.3 11.966,5.322 12.056,5.367C12.153,5.412 12.236,5.468 12.303,5.536L14.527,7.804C14.661,7.947 14.729,8.126 14.729,8.343C14.736,8.561 14.669,8.737 14.527,8.871L12.281,11.117C12.131,11.267 11.962,11.342 11.775,11.342C11.566,11.342 11.39,11.271 11.247,11.129C11.105,10.986 11.034,10.81 11.034,10.601C11.034,10.399 11.109,10.223 11.259,10.073L12.37,8.961C12.266,8.946 12.131,8.939 11.966,8.939C11.307,8.939 10.708,9.1 10.169,9.422C9.63,9.736 9.2,10.163 8.878,10.702C8.563,11.241 8.406,11.84 8.406,12.499C8.406,13.165 8.563,13.768 8.878,14.307C9.2,14.846 9.63,15.276 10.169,15.598C10.708,15.92 11.307,16.081 11.966,16.081C12.625,16.081 13.224,15.92 13.763,15.598C14.302,15.276 14.729,14.846 15.043,14.307C15.365,13.768 15.526,13.165 15.526,12.499C15.526,12.282 15.605,12.098 15.762,11.948C15.919,11.791 16.11,11.713 16.335,11.713C16.544,11.713 16.728,11.791 16.885,11.948C17.05,12.098 17.132,12.282 17.132,12.499C17.132,13.45 16.9,14.318 16.436,15.104C15.972,15.89 15.346,16.519 14.56,16.991C13.782,17.455 12.917,17.687 11.966,17.687C11.008,17.687 10.139,17.455 9.361,16.991C8.582,16.519 7.961,15.887 7.496,15.093Z").toNodes(),
                fill = SolidColor(Color(0xFFF59E0B)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val refreshOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_refresh_outline",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10,1.999C12.515,1.999 14.758,3.161 16.224,4.974L17.407,3.791C17.596,3.602 17.919,3.736 17.919,4.003V7.744C17.919,7.91 17.785,8.044 17.619,8.044H13.878C13.611,8.044 13.477,7.721 13.666,7.532L15.084,6.113C13.913,4.584 12.072,3.599 10,3.599C6.465,3.599 3.599,6.465 3.599,10C3.599,13.535 6.465,16.401 10,16.401C13.535,16.401 16.401,13.535 16.401,10H18.001C18.001,14.419 14.419,18.001 10,18.001C5.581,18.001 1.999,14.419 1.999,10C1.999,5.581 5.581,1.999 10,1.999Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val refreshOutline14: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_refresh_outline_14",
            defaultWidth = 13.dp,
            defaultHeight = 13.dp,
            viewportWidth = 13f,
            viewportHeight = 13f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M1.845,5.849C2.204,3.266 4.589,1.464 7.173,1.823C8.197,1.965 9.098,2.427 9.79,3.093L10.598,2.286C10.752,2.133 11.014,2.241 11.014,2.458V4.946C11.014,5.08 10.905,5.189 10.77,5.189H9.946L9.871,5.218C9.867,5.208 9.863,5.199 9.859,5.189H8.283C8.066,5.189 7.958,4.926 8.111,4.773L8.986,3.897C8.463,3.4 7.785,3.056 7.016,2.949C5.055,2.676 3.244,4.046 2.971,6.007C2.699,7.967 4.068,9.778 6.029,10.051C7.99,10.323 9.801,8.954 10.073,6.994L10.637,7.071L11.2,7.15C10.841,9.733 8.455,11.536 5.872,11.177C3.289,10.818 1.486,8.433 1.845,5.849Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val refreshOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_refresh_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10,2C12.515,2 14.758,3.162 16.224,4.975L17.407,3.792C17.596,3.603 17.919,3.737 17.919,4.004V7.745C17.919,7.911 17.785,8.045 17.619,8.045H13.878C13.611,8.045 13.477,7.722 13.666,7.533L15.084,6.114C13.913,4.585 12.072,3.6 10,3.6C6.465,3.6 3.599,6.466 3.599,10.001C3.599,13.536 6.465,16.402 10,16.402C13.535,16.402 16.401,13.536 16.401,10.001H18.001C18.001,14.42 14.419,18.002 10,18.002C5.581,18.002 1.999,14.42 1.999,10.001C1.999,5.582 5.581,2 10,2Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val regegerateOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_regegerate_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M3.37,12.521C3.21,12.481 3.046,12.578 3.005,12.738L2.125,16.206C2.059,16.465 2.34,16.674 2.569,16.538L3.804,15.802C4.899,18.182 7.02,20.067 9.76,20.763C14.572,21.983 19.463,19.072 20.684,14.26L18.938,13.817C17.961,17.665 14.05,19.994 10.202,19.017C7.946,18.445 6.212,16.864 5.36,14.876L6.917,13.95C7.147,13.813 7.097,13.467 6.838,13.401L3.37,12.521Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M14.275,3.36C9.463,2.139 4.572,5.051 3.352,9.863L5.098,10.306C6.074,6.457 9.985,4.129 13.833,5.105C16.088,5.678 17.823,7.258 18.675,9.246L17.113,10.176C16.884,10.312 16.934,10.659 17.193,10.724L20.668,11.606C20.828,11.647 20.992,11.549 21.032,11.389L21.914,7.914C21.979,7.655 21.699,7.446 21.47,7.583L20.232,8.319C19.137,5.939 17.015,4.055 14.275,3.36Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val reportOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_report_outline",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M5.947,2.118C7.165,2.038 8.566,2.226 9.902,2.693C11.417,3.222 12.214,3.557 13.072,3.702C13.877,3.838 14.754,3.804 16.33,3.462C16.566,3.41 16.814,3.468 17.002,3.62C17.19,3.772 17.3,4.001 17.3,4.243V13.035C17.3,13.322 17.145,13.588 16.896,13.73C15.522,14.512 14.365,14.673 13.134,14.446C12.006,14.238 10.774,13.682 9.359,13.154C8.231,12.733 7.062,12.603 6.087,12.71C5.245,12.802 4.644,13.059 4.3,13.356V17.9H2.7V3.841C2.7,3.643 2.773,3.453 2.904,3.306C3.595,2.536 4.77,2.195 5.947,2.118ZM6.052,3.715C5.223,3.769 4.639,3.969 4.3,4.202V11.514C4.798,11.309 5.351,11.18 5.912,11.119C7.145,10.984 8.565,11.15 9.919,11.655C11.5,12.245 12.481,12.698 13.425,12.873C14.155,13.008 14.836,12.962 15.7,12.55V5.211C14.541,5.405 13.669,5.425 12.806,5.279C11.771,5.104 10.762,4.688 9.375,4.203C8.229,3.802 7.042,3.65 6.052,3.715Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val rotateLeftOutlineThin20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_rotate_left_outline_thin_20",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M5.867,9.135L13.725,9.135A2,2 0,0 1,15.725 11.135L15.725,18.993A2,2 0,0 1,13.725 20.993L5.867,20.993A2,2 0,0 1,3.867 18.993L3.867,11.135A2,2 0,0 1,5.867 9.135z").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 2f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M20.133,10.326V8.193C20.133,5.984 18.342,4.193 16.133,4.193H14.835").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFFFFFFFF)),
                strokeLineWidth = 1.6f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.436,4.505C11.196,4.366 11.196,4.02 11.436,3.881L15.04,1.8C15.28,1.661 15.58,1.835 15.58,2.112V6.274C15.58,6.551 15.28,6.724 15.04,6.586L11.436,4.505Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val scanPictureFilled20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_scan_picture_filled_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M2.525,12.5C2.535,12.753 2.547,12.986 2.564,13.202C2.628,13.974 2.747,14.446 2.936,14.816C3.319,15.569 3.931,16.181 4.684,16.565C5.054,16.753 5.526,16.872 6.298,16.935C6.771,16.974 7.324,16.987 8,16.993V18.992C7.288,18.986 6.672,18.973 6.135,18.929C5.239,18.855 4.476,18.702 3.776,18.346C2.647,17.771 1.73,16.853 1.154,15.724C0.798,15.024 0.644,14.261 0.571,13.365C0.549,13.097 0.536,12.809 0.525,12.5H2.525ZM19.475,12.5C19.464,12.809 19.451,13.097 19.429,13.365C19.355,14.261 19.202,15.024 18.846,15.724C18.271,16.853 17.353,17.771 16.224,18.346C15.524,18.702 14.761,18.855 13.865,18.929C13.328,18.973 12.712,18.986 12,18.992V16.993C12.676,16.987 13.229,16.974 13.702,16.935C14.474,16.872 14.946,16.753 15.316,16.565C16.069,16.181 16.681,15.569 17.065,14.816C17.253,14.446 17.372,13.974 17.435,13.202C17.453,12.986 17.465,12.753 17.475,12.5H19.475ZM6.328,9.881C7.477,9.135 8.986,9.269 9.985,10.205C10.099,10.311 10.218,10.449 10.456,10.724L14.36,15.231C13.774,15.271 12.077,15.229 11.161,15.229H9.649C7.484,15.229 6.401,15.229 5.633,14.692C5.346,14.492 5.097,14.243 4.896,13.956C4.519,13.415 4.407,12.718 4.374,11.596C4.365,11.506 4.403,11.418 4.473,11.361L5.773,10.309C6.056,10.08 6.198,9.965 6.328,9.881ZM13.047,5.95C14.232,5.95 15.193,6.911 15.193,8.097C15.193,9.282 14.232,10.243 13.047,10.243C11.861,10.243 10.9,9.282 10.9,8.097C10.9,6.911 11.861,5.95 13.047,5.95ZM8,3.006C7.324,3.012 6.771,3.026 6.298,3.064C5.526,3.128 5.054,3.247 4.684,3.436C3.931,3.819 3.319,4.431 2.936,5.184C2.747,5.554 2.628,6.026 2.564,6.798C2.547,7.014 2.535,7.247 2.525,7.5H0.525C0.536,7.191 0.549,6.903 0.571,6.635C0.644,5.739 0.798,4.976 1.154,4.276C1.73,3.147 2.647,2.23 3.776,1.654C4.476,1.298 5.239,1.144 6.135,1.071C6.672,1.027 7.288,1.013 8,1.007V3.006ZM12,1.007C12.712,1.013 13.328,1.027 13.865,1.071C14.761,1.144 15.524,1.298 16.224,1.654C17.353,2.23 18.271,3.147 18.846,4.276C19.202,4.976 19.355,5.739 19.429,6.635C19.451,6.903 19.464,7.191 19.475,7.5H17.475C17.465,7.247 17.453,7.014 17.435,6.798C17.372,6.026 17.253,5.554 17.065,5.184C16.681,4.431 16.069,3.819 15.316,3.436C14.946,3.247 14.474,3.128 13.702,3.064C13.229,3.026 12.676,3.012 12,3.006V1.007Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val scanTextFilled20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_scan_text_filled_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M2.525,12.5C2.535,12.753 2.547,12.986 2.564,13.202C2.628,13.974 2.747,14.446 2.936,14.816C3.319,15.569 3.931,16.181 4.684,16.565C5.054,16.753 5.526,16.872 6.298,16.935C6.771,16.974 7.324,16.987 8,16.993V18.992C7.288,18.986 6.672,18.973 6.135,18.929C5.239,18.855 4.476,18.702 3.776,18.346C2.647,17.771 1.73,16.853 1.154,15.724C0.798,15.024 0.644,14.261 0.571,13.365C0.549,13.097 0.536,12.809 0.525,12.5H2.525ZM19.475,12.5C19.464,12.809 19.451,13.097 19.429,13.365C19.355,14.261 19.202,15.024 18.846,15.724C18.271,16.853 17.353,17.771 16.224,18.346C15.524,18.702 14.761,18.855 13.865,18.929C13.328,18.973 12.712,18.986 12,18.992V16.993C12.676,16.987 13.229,16.974 13.702,16.935C14.474,16.872 14.946,16.753 15.316,16.565C16.069,16.181 16.681,15.569 17.065,14.816C17.253,14.446 17.372,13.974 17.435,13.202C17.453,12.986 17.465,12.753 17.475,12.5H19.475ZM9.226,5.398C9.573,4.821 10.43,4.821 10.777,5.398L10.845,5.53L14.352,14.117H12.626L11.727,11.914H8.276L7.377,14.117H5.652L9.158,5.53L9.226,5.398ZM8.93,10.316H11.074L10.002,7.69L8.93,10.316ZM8,3.006C7.324,3.012 6.771,3.026 6.298,3.064C5.526,3.128 5.054,3.247 4.684,3.436C3.931,3.819 3.319,4.431 2.936,5.184C2.747,5.554 2.628,6.026 2.564,6.798C2.547,7.014 2.535,7.247 2.525,7.5H0.525C0.536,7.191 0.549,6.903 0.571,6.635C0.644,5.739 0.798,4.976 1.154,4.276C1.73,3.147 2.647,2.23 3.776,1.654C4.476,1.298 5.239,1.144 6.135,1.071C6.672,1.027 7.288,1.013 8,1.007V3.006ZM12,1.007C12.712,1.013 13.328,1.027 13.865,1.071C14.761,1.144 15.524,1.298 16.224,1.654C17.353,2.23 18.271,3.147 18.846,4.276C19.202,4.976 19.355,5.739 19.429,6.635C19.451,6.903 19.464,7.191 19.475,7.5H17.475C17.465,7.247 17.453,7.014 17.435,6.798C17.372,6.026 17.253,5.554 17.065,5.184C16.681,4.431 16.069,3.819 15.316,3.436C14.946,3.247 14.474,3.128 13.702,3.064C13.229,3.026 12.676,3.012 12,3.006V1.007Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val searchOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_search_outline_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M11.686,6.611C11.686,3.853 9.45,1.617 6.692,1.617C3.934,1.617 1.698,3.853 1.698,6.611C1.698,9.369 3.934,11.605 6.692,11.605C9.45,11.605 11.686,9.369 11.686,6.611ZM13.025,6.611C13.025,10.108 10.189,12.944 6.692,12.944C3.195,12.944 0.359,10.108 0.359,6.611C0.359,3.114 3.195,0.278 6.692,0.278C10.189,0.278 13.025,3.114 13.025,6.611Z").toNodes(),
                fill = SolidColor(Color(0xFF3964FE)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M15.641,14.557L14.694,15.505L11.347,12.158L12.294,11.211L15.641,14.557Z").toNodes(),
                fill = SolidColor(Color(0xFF3964FE)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val searchWarningOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_search_warning_outline_16",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M27.166,25.519L25.433,27.252L20.183,22.002L21.916,20.269L27.166,25.519Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M6.911,4.844C4.9,6.271 3.52,8.544 3.274,11.136L2.054,11.02L0.834,10.906C1.149,7.582 2.919,4.675 5.493,2.848L6.911,4.844Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M3.274,12.882C3.519,15.47 4.898,17.729 6.916,19.178L5.488,21.169C2.921,19.327 1.149,16.441 0.834,13.114L3.274,12.882Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.177,21.136L16.686,22.25C15.266,22.901 13.692,23.253 12.029,23.253C10.367,23.253 8.792,22.901 7.371,22.25L8.393,20.021C9.492,20.525 10.717,20.802 12.029,20.802C13.342,20.802 14.567,20.525 15.666,20.021L16.177,21.136Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M23.24,13.097C22.926,16.42 21.157,19.327 18.583,21.155L17.165,19.157C19.176,17.729 20.556,15.456 20.802,12.864L23.24,13.097Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.588,2.851C21.154,4.692 22.925,7.579 23.24,10.906L22.022,11.02L20.802,11.136C20.557,8.548 19.179,6.289 17.16,4.84L18.588,2.851Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M12.029,0.748C13.692,0.748 15.266,1.102 16.686,1.753L15.666,3.979C14.567,3.476 13.342,3.199 12.029,3.198C10.717,3.198 9.492,3.476 8.393,3.979L7.371,1.753C8.792,1.101 10.367,0.748 12.029,0.748Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val searchWeb: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_search_web",
            defaultWidth = 22.dp,
            defaultHeight = 22.dp,
            viewportWidth = 22f,
            viewportHeight = 22f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M19.178,11.77H2.31V10.23H19.178V11.77Z").toNodes(),
                fill = SolidColor(Color(0xFFF8F8F8)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.31,2.944C9.691,4.689 8.58,7.616 8.58,11C8.58,14.384 9.691,17.311 11.31,19.056L10.18,20.104C8.244,18.015 7.04,14.685 7.04,11C7.04,7.315 8.244,3.985 10.18,1.896L11.31,2.944Z").toNodes(),
                fill = SolidColor(Color(0xFFF8F8F8)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M10.8,2.944C12.419,4.689 13.53,7.616 13.53,11C13.53,14.384 12.419,17.311 10.8,19.056L11.93,20.104C13.866,18.015 15.07,14.685 15.07,11C15.07,7.315 13.866,3.985 11.93,1.896L10.8,2.944Z").toNodes(),
                fill = SolidColor(Color(0xFFF8F8F8)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.055,18.92C15.399,18.92 18.92,15.399 18.92,11.055C18.92,6.711 15.399,3.19 11.055,3.19C6.711,3.19 3.19,6.711 3.19,11.055C3.19,15.399 6.711,18.92 11.055,18.92ZM11.055,20.35C16.188,20.35 20.35,16.188 20.35,11.055C20.35,5.921 16.188,1.76 11.055,1.76C5.922,1.76 1.76,5.921 1.76,11.055C1.76,16.188 5.922,20.35 11.055,20.35Z").toNodes(),
                fill = SolidColor(Color(0xFFF8F8F8)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val selectAllOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_select_all_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M16.4236 9.99976C16.4236 8.4707 16.4228 7.38859 16.3318 6.55444C16.2424 5.73548 16.0737 5.25192 15.8035 4.88355C15.6108 4.62092 15.3796 4.38874 15.1169 4.19605C14.7486 3.9258 14.2648 3.75809 13.446 3.6687C12.6118 3.57766 11.529 3.57593 9.99976 3.57593C8.47067 3.57593 7.3886 3.57768 6.55444 3.6687C5.73565 3.75806 5.25189 3.92589 4.88355 4.19605C4.62092 4.38874 4.38874 4.62092 4.19605 4.88355C3.92589 5.25189 3.75806 5.73565 3.6687 6.55444C3.57768 7.3886 3.57593 8.47067 3.57593 9.99976C3.57593 11.529 3.57766 12.6118 3.6687 13.446C3.75809 14.2648 3.9258 14.7486 4.19605 15.1169C4.38874 15.3796 4.62092 15.6108 4.88355 15.8035C5.25192 16.0737 5.73548 16.2424 6.55444 16.3318C7.38859 16.4228 8.4707 16.4236 9.99976 16.4236C11.529 16.4236 12.6118 16.4228 13.446 16.3318C14.2649 16.2424 14.7486 16.0737 15.1169 15.8035C15.3794 15.6109 15.6109 15.3794 15.8035 15.1169C16.0737 14.7486 16.2424 14.2649 16.3318 13.446C16.4228 12.6118 16.4236 11.529 16.4236 9.99976ZM18.0242 9.99976C18.0242 11.4929 18.0253 12.6778 17.9226 13.6189C17.8182 14.5752 17.5982 15.3754 17.0935 16.0632C16.8045 16.4571 16.4571 16.8045 16.0632 17.0935C15.3754 17.5982 14.5752 17.8182 13.6189 17.9226C12.6778 18.0253 11.4929 18.0242 9.99976 18.0242C8.50652 18.0242 7.32172 18.0253 6.38062 17.9226C5.42432 17.8182 4.62411 17.5982 3.93628 17.0935C3.5425 16.8045 3.19496 16.457 2.90601 16.0632C2.4014 15.3754 2.18224 14.5752 2.07788 13.6189C1.97521 12.6778 1.97632 11.4929 1.97632 9.99976C1.97632 8.50652 1.97518 7.32172 2.07788 6.38062C2.18225 5.42434 2.40133 4.6241 2.90601 3.93628C3.19499 3.54245 3.54245 3.19499 3.93628 2.90601C4.6241 2.40133 5.42434 2.18225 6.38062 2.07788C7.32172 1.97518 8.50652 1.97632 9.99976 1.97632C11.4929 1.97632 12.6778 1.97521 13.6189 2.07788C14.5752 2.18224 15.3754 2.4014 16.0632 2.90601C16.457 3.19496 16.8045 3.5425 17.0935 3.93628C17.5982 4.62411 17.8182 5.42432 17.9226 6.38062C18.0253 7.32172 18.0242 8.50652 18.0242 9.99976Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M15.0189 7.11611L10.9847 12.3212C10.7489 12.6254 10.5319 12.9065 10.3284 13.1161C10.1149 13.3361 9.8462 13.5538 9.47689 13.6483C9.28446 13.6976 9.08504 13.714 8.88704 13.6981C8.50705 13.6677 8.2052 13.4997 7.95736 13.3192C7.72117 13.1472 7.45929 12.9072 7.17513 12.6474L5.07357 10.7255L6.15364 9.54482L8.25521 11.4657C8.56662 11.7504 8.75164 11.9191 8.89876 12.0263C8.96493 12.0745 9.00355 12.0946 9.02083 12.1024C9.03837 12.1033 9.05543 12.1013 9.07259 12.0976C9.08793 12.0873 9.12304 12.0616 9.18099 12.0019C9.30773 11.8712 9.46163 11.6742 9.72005 11.3407L13.7542 6.13564L15.0189 7.11611Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val selectOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_select_outline",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M6.45,3.8H5.039V16.2H6.45V17.8H1.95V16.2H3.439V3.8H1.95V2.2H6.45V3.8Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M10.05,17.8H8.45V16.2H10.05V17.8Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M14.05,17.8H12.45V16.2H14.05V17.8Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.05,17.8H16.45V16.2H18.05V17.8Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.05,14.3H16.45V12.7H18.05V14.3Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.05,10.803H16.45V9.203H18.05V10.803Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.05,7.3H16.45V5.7H18.05V7.3Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M10.05,3.8H8.45V2.2H10.05V3.8Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M14.05,3.8H12.45V2.2H14.05V3.8Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.05,3.8H16.45V2.2H18.05V3.8Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val sendFill20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_send_fill_20",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M9.065,2.32V14.628H6.932V2.32H9.065Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.153,1.381L8.306,1.399L8.451,1.429C8.781,1.511 9.037,1.68 9.234,1.838C9.442,2.007 9.667,2.232 9.885,2.45L14.655,7.22L13.147,8.729L8.377,3.959C8.257,3.839 8.161,3.745 8.078,3.665C8.049,3.637 8.022,3.614 7.999,3.593C7.976,3.614 7.949,3.637 7.92,3.665C7.837,3.745 7.742,3.839 7.622,3.959L2.852,8.729L1.344,7.22L6.114,2.45C6.333,2.232 6.557,2.007 6.766,1.838C6.99,1.658 7.292,1.463 7.694,1.399L7.846,1.381C7.948,1.373 8.051,1.373 8.153,1.381Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val shareOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_share_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M9.741,2.914C9.742,2.111 10.659,1.693 11.262,2.153L11.377,2.257L17.909,9.068C18.41,9.59 18.41,10.415 17.909,10.937L11.377,17.744C10.785,18.361 9.741,17.941 9.741,17.086V13.284C8.646,13.337 6.167,13.827 3.01,16.759C2.776,16.975 2.46,17.01 2.208,16.914C1.948,16.813 1.714,16.551 1.715,16.193L1.724,15.791C1.802,13.758 2.409,11.388 3.703,9.462C4.991,7.544 6.972,6.06 9.741,5.832V2.914ZM11.241,6.53C11.241,6.973 10.879,7.3 10.473,7.301C7.898,7.307 6.117,8.56 4.948,10.299C4.106,11.553 3.591,13.055 3.359,14.493C7.312,11.425 10.191,11.754 10.602,11.823C11.06,11.9 11.241,12.303 11.241,12.582V15.719L16.728,10.002L11.241,4.281V6.53Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val shortcutSendPhoto: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_shortcut_send_photo",
            defaultWidth = 48.dp,
            defaultHeight = 48.dp,
            viewportWidth = 48f,
            viewportHeight = 48f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M18.575,15.105C15.66,15.105 13.296,17.469 13.296,20.383C13.296,23.298 15.66,25.662 18.575,25.662C21.49,25.662 23.854,23.299 23.854,20.383C23.854,17.468 21.49,15.105 18.575,15.105ZM18.575,18.431C19.654,18.431 20.528,19.305 20.528,20.383C20.528,21.462 19.654,22.336 18.575,22.336C17.497,22.336 16.622,21.462 16.622,20.383C16.622,19.305 17.497,18.431 18.575,18.431Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M22.191,9.68C19.637,9.68 17.591,9.677 15.959,9.849C14.297,10.024 12.873,10.395 11.629,11.264C10.783,11.855 10.046,12.589 9.455,13.435C8.587,14.679 8.218,16.103 8.043,17.765C7.871,19.397 7.874,21.444 7.874,23.999C7.874,26.553 7.871,28.599 8.043,30.231C8.218,31.893 8.587,33.318 9.455,34.562C10.046,35.407 10.783,36.144 11.629,36.735C12.873,37.603 14.297,37.972 15.959,38.147C17.591,38.319 19.637,38.317 22.191,38.317H25.807C28.362,38.317 30.409,38.319 32.041,38.147C33.703,37.972 35.127,37.604 36.371,36.735C37.217,36.144 37.951,35.407 38.542,34.562C39.411,33.317 39.782,31.893 39.957,30.231C40.129,28.599 40.126,26.553 40.126,23.999C40.126,21.444 40.129,19.397 39.957,17.765C39.782,16.103 39.411,14.679 38.542,13.435C37.952,12.59 37.216,11.854 36.371,11.264C35.127,10.395 33.703,10.024 32.041,9.849C30.409,9.677 28.361,9.68 25.807,9.68H22.191ZM29.501,26.692C30.772,26.071 32.235,25.967 33.581,26.402C34.292,26.632 35.016,27.12 36.749,28.411C36.727,28.951 36.696,29.438 36.65,29.883C36.504,31.272 36.232,32.064 35.817,32.657C35.45,33.183 34.993,33.64 34.467,34.007C33.873,34.422 33.081,34.694 31.693,34.84C30.275,34.989 28.433,34.993 25.807,34.993H22.191C21.698,34.993 21.232,34.985 20.792,34.984L26.015,29.76C28.016,27.76 28.744,27.062 29.501,26.692ZM25.807,13.006C28.433,13.006 30.275,13.007 31.693,13.156C33.081,13.302 33.874,13.575 34.467,13.989C34.993,14.356 35.45,14.814 35.817,15.339C36.232,15.932 36.504,16.725 36.65,18.113C36.799,19.531 36.8,21.373 36.8,23.999C36.8,24.118 36.8,24.236 36.8,24.352C36.03,23.845 35.331,23.474 34.604,23.239C32.438,22.538 30.085,22.705 28.04,23.705C26.662,24.379 25.48,25.593 23.664,27.409L16.242,34.831C14.894,34.683 14.117,34.415 13.533,34.007C13.007,33.64 12.55,33.183 12.183,32.657C11.769,32.064 11.496,31.271 11.35,29.883C11.201,28.466 11.197,26.625 11.197,23.999C11.197,21.373 11.201,19.531 11.35,18.113C11.497,16.725 11.768,15.932 12.183,15.339C12.55,14.813 13.007,14.356 13.533,13.989C14.127,13.575 14.919,13.302 16.307,13.156C17.725,13.007 19.565,13.006 22.191,13.006H25.807Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val simplifyOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_simplify_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M17.256,6.422V7.922H2.744V6.422H17.256Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M10.967,12.077V13.577H2.744V12.077H10.967Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val siteIconPlaceholder: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_site_icon_placeholder",
            defaultWidth = 12.dp,
            defaultHeight = 12.dp,
            viewportWidth = 11f,
            viewportHeight = 11f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M6.579,4.473C7.524,5.418 7.525,6.951 6.58,7.896L5.453,9.022C4.508,9.968 2.974,9.968 2.028,9.023C1.083,8.077 1.083,6.543 2.028,5.598L3.138,4.49C3.062,4.927 3.081,5.383 3.204,5.825L2.729,6.298C2.17,6.857 2.17,7.763 2.729,8.322C3.288,8.881 4.194,8.881 4.753,8.322L5.879,7.196C6.438,6.637 6.438,5.731 5.88,5.172C5.745,5.038 5.59,4.936 5.425,4.867L6.157,4.135C6.307,4.23 6.449,4.342 6.579,4.473Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M4.478,6.575C3.534,5.629 3.533,4.097 4.478,3.152L5.601,2.029C6.547,1.084 8.079,1.084 9.024,2.028C9.969,2.974 9.969,4.507 9.024,5.452L7.915,6.562C7.981,6.129 7.954,5.68 7.828,5.248L8.324,4.752C8.882,4.193 8.883,3.287 8.324,2.729C7.766,2.17 6.86,2.17 6.301,2.729L5.178,3.852C4.62,4.41 4.62,5.316 5.178,5.875C5.31,6.006 5.461,6.106 5.622,6.175L4.891,6.905C4.745,6.812 4.606,6.702 4.478,6.575Z").toNodes(),
                fill = SolidColor(Color(0xFF8E9094)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val sizeRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_size_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12.294,17.976C12.017,17.976 11.798,17.902 11.638,17.752C11.483,17.603 11.406,17.395 11.406,17.128C11.406,16.968 11.441,16.784 11.51,16.576L15.07,6.976C15.305,6.31 15.769,5.976 16.462,5.976C16.83,5.976 17.129,6.059 17.358,6.224C17.593,6.39 17.771,6.64 17.894,6.976L21.43,16.536C21.505,16.744 21.542,16.936 21.542,17.112C21.542,17.379 21.457,17.59 21.286,17.744C21.121,17.899 20.897,17.976 20.614,17.976C20.347,17.976 20.137,17.915 19.982,17.792C19.833,17.675 19.715,17.472 19.63,17.184L18.742,14.704H14.174L13.286,17.184C13.201,17.472 13.081,17.675 12.926,17.792C12.777,17.915 12.566,17.976 12.294,17.976ZM14.638,13.216H18.294L16.486,8.016H16.446L14.638,13.216ZM3.302,17.976C3.051,17.976 2.851,17.907 2.702,17.768C2.558,17.635 2.486,17.448 2.486,17.208C2.486,17.123 2.494,17.03 2.51,16.928C2.531,16.827 2.561,16.728 2.598,16.632L5.014,10.152C5.137,9.832 5.299,9.598 5.502,9.448C5.705,9.294 5.953,9.216 6.246,9.216C6.555,9.216 6.814,9.294 7.022,9.448C7.23,9.603 7.393,9.838 7.51,10.152L9.926,16.632C9.969,16.739 10.001,16.838 10.022,16.928C10.043,17.019 10.054,17.112 10.054,17.208C10.054,17.438 9.977,17.622 9.822,17.76C9.673,17.904 9.475,17.976 9.23,17.976C9.017,17.976 8.841,17.92 8.702,17.808C8.569,17.702 8.465,17.52 8.39,17.264L7.814,15.544H4.702L4.134,17.248C4.054,17.504 3.947,17.688 3.814,17.8C3.686,17.918 3.515,17.976 3.302,17.976ZM5.094,14.288H7.422L6.334,10.952H6.166L5.094,14.288Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val stopFill20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_stop_fill_20",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M4.889,2.223L11.112,2.223A2.667,2.667 0,0 1,13.778 4.889L13.778,11.112A2.667,2.667 0,0 1,11.112 13.778L4.889,13.778A2.667,2.667 0,0 1,2.223 11.112L2.223,4.889A2.667,2.667 0,0 1,4.889 2.223z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val stopSpeechOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_stop_speech_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M16.76,10C16.76,6.266 13.733,3.24 10,3.24C6.266,3.24 3.24,6.266 3.24,10C3.24,13.733 6.266,16.76 10,16.76C13.733,16.76 16.76,13.733 16.76,10ZM18.36,10C18.36,14.617 14.617,18.36 10,18.36C5.383,18.36 1.64,14.616 1.64,10C1.64,5.383 5.383,1.64 10,1.64C14.616,1.64 18.36,5.383 18.36,10Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M7.416,8.416C7.416,7.863 7.863,7.416 8.416,7.416H11.584C12.137,7.416 12.584,7.863 12.584,8.416V11.584C12.584,12.137 12.137,12.584 11.584,12.584H8.416C7.863,12.584 7.416,12.137 7.416,11.584V8.416Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val sunRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_sun_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M12,9.341C10.531,9.341 9.341,10.531 9.341,12C9.341,13.469 10.531,14.659 12,14.659C13.469,14.659 14.659,13.469 14.659,12C14.659,10.531 13.469,9.341 12,9.341ZM12,7.568C9.552,7.568 7.568,9.552 7.568,12C7.568,14.448 9.552,16.432 12,16.432C14.448,16.432 16.432,14.448 16.432,12C16.432,9.552 14.448,7.568 12,7.568Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.248,7.752C15.902,7.406 15.902,6.845 16.248,6.498L17.641,5.106C17.987,4.76 18.548,4.76 18.894,5.106C19.24,5.452 19.24,6.013 18.894,6.359L17.501,7.752C17.155,8.098 16.594,8.098 16.248,7.752Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.205,12C18.205,11.51 18.601,11.114 19.091,11.114H20.864C21.353,11.114 21.75,11.51 21.75,12C21.75,12.49 21.353,12.886 20.864,12.886H19.091C18.601,12.886 18.205,12.49 18.205,12Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M7.752,7.752C7.406,8.098 6.845,8.098 6.499,7.752L5.106,6.359C4.76,6.013 4.76,5.452 5.106,5.106C5.452,4.76 6.013,4.76 6.359,5.106L7.752,6.498C8.098,6.845 8.098,7.406 7.752,7.752Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M12,5.795C11.51,5.795 11.114,5.399 11.114,4.909V3.136C11.114,2.647 11.51,2.25 12,2.25C12.49,2.25 12.886,2.647 12.886,3.136V4.909C12.886,5.399 12.49,5.795 12,5.795Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M5.106,18.894C4.76,18.548 4.76,17.987 5.106,17.641L6.498,16.248C6.845,15.902 7.406,15.902 7.752,16.248C8.098,16.594 8.098,17.155 7.752,17.501L6.359,18.894C6.013,19.24 5.452,19.24 5.106,18.894Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M2.25,12C2.25,11.51 2.647,11.114 3.136,11.114H4.909C5.399,11.114 5.795,11.51 5.795,12C5.795,12.49 5.399,12.886 4.909,12.886H3.136C2.647,12.886 2.25,12.49 2.25,12Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.894,18.894C18.548,19.24 17.987,19.24 17.641,18.894L16.248,17.501C15.902,17.155 15.902,16.594 16.248,16.248C16.594,15.902 17.155,15.902 17.502,16.248L18.894,17.641C19.24,17.987 19.24,18.548 18.894,18.894Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M12,21.75C11.51,21.75 11.114,21.353 11.114,20.864V19.091C11.114,18.601 11.51,18.205 12,18.205C12.49,18.205 12.886,18.601 12.886,19.091V20.864C12.886,21.353 12.49,21.75 12,21.75Z").toNodes(),
                fill = SolidColor(Color(0xFF18181B)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val textModeOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_text_mode_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10.738,6.067H15.245V7.567H13.579C13.322,9.606 12.458,11.444 11.147,12.846C12.309,13.639 13.715,14.098 15.27,14.098V15.598C13.248,15.598 11.447,14.955 10,13.872C8.552,14.955 6.751,15.598 4.73,15.598V14.098C6.284,14.098 7.69,13.638 8.852,12.846C7.541,11.444 6.678,9.606 6.421,7.567H4.73V6.067H9.238L9.237,4.402H10.737L10.738,6.067ZM7.933,7.567C8.181,9.251 8.916,10.743 10,11.875C11.084,10.743 11.819,9.251 12.067,7.567H7.933Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val thinkOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_think_outline_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M8.002,6.769C8.682,6.769 9.232,7.321 9.232,8C9.232,8.679 8.682,9.229 8.002,9.229C7.323,9.229 6.772,8.679 6.772,8C6.772,7.321 7.323,6.769 8.002,6.769Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M9.936,1.307C11.54,0.73 13.239,0.645 14.297,1.703C15.355,2.761 15.27,4.461 14.693,6.065C14.467,6.691 14.154,7.342 13.763,8C14.154,8.658 14.467,9.309 14.693,9.935C15.27,11.539 15.355,13.239 14.297,14.297C13.239,15.355 11.54,15.269 9.936,14.692C9.31,14.467 8.658,14.153 8,13.763C7.343,14.153 6.692,14.467 6.066,14.692C4.462,15.27 2.762,15.355 1.703,14.297C0.645,13.238 0.731,11.539 1.308,9.935C1.533,9.309 1.845,8.658 2.236,8C1.845,7.342 1.533,6.691 1.308,6.065C0.731,4.461 0.645,2.761 1.703,1.703C2.762,0.645 4.462,0.73 6.066,1.307C6.692,1.533 7.343,1.845 8,2.235C8.658,1.845 9.31,1.533 9.936,1.307ZM3.11,9.307C2.914,9.685 2.753,10.055 2.625,10.409C2.091,11.893 2.243,12.858 2.693,13.307C3.143,13.757 4.107,13.909 5.591,13.375C5.945,13.247 6.314,13.084 6.693,12.889C6.041,12.402 5.395,11.845 4.775,11.225C4.154,10.605 3.596,9.959 3.11,9.307ZM12.889,9.307C12.403,9.959 11.846,10.605 11.226,11.225C10.606,11.846 9.959,12.402 9.307,12.889C9.686,13.084 10.055,13.247 10.41,13.375C11.894,13.909 12.858,13.757 13.308,13.307C13.758,12.858 13.91,11.893 13.375,10.409C13.248,10.055 13.085,9.685 12.889,9.307ZM7.999,3.897C7.253,4.409 6.495,5.035 5.765,5.765C5.035,6.494 4.409,7.252 3.898,7.999C4.409,8.746 5.035,9.505 5.765,10.235C6.495,10.965 7.253,11.59 7.999,12.101C8.746,11.59 9.506,10.965 10.236,10.235C10.966,9.505 11.59,8.746 12.102,7.999C11.59,7.252 10.965,6.494 10.236,5.765C9.506,5.034 8.746,4.409 7.999,3.897ZM5.591,2.625C4.107,2.091 3.142,2.243 2.693,2.692C2.243,3.142 2.091,4.106 2.625,5.591C2.753,5.945 2.914,6.314 3.11,6.692C3.596,6.04 4.154,5.395 4.775,4.774C5.395,4.154 6.04,3.596 6.693,3.109C6.314,2.914 5.945,2.752 5.591,2.625ZM13.308,2.692C12.858,2.243 11.894,2.091 10.41,2.625C10.055,2.752 9.686,2.914 9.307,3.109C9.959,3.596 10.606,4.154 11.226,4.774C11.846,5.394 12.403,6.04 12.889,6.692C13.085,6.314 13.248,5.945 13.375,5.591C13.91,4.107 13.757,3.142 13.308,2.692Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val tileCameraAsk: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_tile_camera_ask",
            defaultWidth = 22.dp,
            defaultHeight = 22.dp,
            viewportWidth = 22f,
            viewportHeight = 22f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M18.865,9.218C18.865,8.67 18.863,8.487 18.844,8.347C18.702,7.29 17.87,6.458 16.813,6.317C16.453,6.269 16.087,6.315 15.727,6.291C15.274,6.261 14.844,6.079 14.506,5.777C14.22,5.521 13.964,5.227 13.696,4.953C13.397,4.646 13.094,4.244 12.669,4.104C12.509,4.051 12.322,4.04 11.531,4.04H10.469C9.678,4.04 9.491,4.051 9.331,4.104C8.906,4.244 8.603,4.646 8.304,4.953C8.036,5.227 7.78,5.521 7.494,5.777C7.156,6.079 6.726,6.261 6.273,6.291C5.912,6.315 5.547,6.269 5.187,6.317C4.13,6.458 3.297,7.29 3.156,8.347C3.137,8.487 3.135,8.67 3.135,9.218V12.955C3.135,14.213 3.137,15.081 3.209,15.746C3.279,16.394 3.407,16.737 3.587,16.985C3.733,17.186 3.909,17.362 4.11,17.508C4.358,17.688 4.701,17.816 5.349,17.886C6.014,17.958 6.882,17.959 8.14,17.959H13.86C15.118,17.959 15.986,17.958 16.652,17.886C17.299,17.816 17.642,17.688 17.89,17.508C18.091,17.362 18.267,17.186 18.413,16.985C18.593,16.737 18.721,16.394 18.791,15.746C18.863,15.081 18.865,14.213 18.865,12.955V9.218ZM20.735,12.955C20.735,14.171 20.736,15.159 20.651,15.948C20.563,16.755 20.376,17.464 19.926,18.084C19.664,18.443 19.348,18.76 18.989,19.021C18.369,19.471 17.66,19.658 16.853,19.746C16.064,19.831 15.076,19.83 13.86,19.83H8.14C6.924,19.83 5.936,19.831 5.147,19.746C4.34,19.658 3.631,19.471 3.011,19.021C2.652,18.76 2.335,18.443 2.074,18.084C1.624,17.464 1.437,16.755 1.349,15.948C1.264,15.159 1.265,14.171 1.265,12.955V9.218C1.265,8.73 1.263,8.395 1.302,8.1C1.555,6.206 3.045,4.716 4.939,4.463C5.234,4.423 5.57,4.425 6.058,4.425C6.17,4.425 6.228,4.403 6.309,4.32L6.965,3.647C7.433,3.168 7.804,2.771 8.283,2.523C8.431,2.446 8.586,2.381 8.744,2.328C9.128,2.201 9.53,2.175 9.989,2.17H12.011C12.47,2.175 12.872,2.201 13.256,2.328C13.414,2.381 13.569,2.446 13.717,2.523C14.196,2.771 14.567,3.168 15.035,3.647L15.691,4.32C15.768,4.398 15.828,4.425 15.943,4.425C16.43,4.425 16.766,4.423 17.061,4.463C18.955,4.716 20.445,6.206 20.698,8.1C20.737,8.395 20.735,8.73 20.735,9.218V12.955Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M13.365,11.8C13.365,10.494 12.306,9.435 11,9.435C9.694,9.435 8.635,10.494 8.635,11.8C8.635,13.106 9.694,14.165 11,14.165C12.306,14.165 13.365,13.106 13.365,11.8ZM15.235,11.8C15.235,14.139 13.339,16.035 11,16.035C8.661,16.035 6.765,14.139 6.765,11.8C6.765,9.461 8.661,7.565 11,7.565C13.339,7.565 15.235,9.461 15.235,11.8Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val tileNewChat: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_tile_new_chat",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M1.6,12C1.6,6.256 6.256,1.6 12,1.6C17.744,1.6 22.4,6.256 22.4,12C22.4,17.744 17.744,22.4 12,22.4C10.953,22.4 10.059,22.327 9.206,22.092C8.343,21.854 7.568,21.462 6.735,20.881C6.15,20.473 5.478,20.437 5.024,20.72L3.503,21.792L2.906,20.011L4.021,19.227L4.058,19.201C5.268,18.437 6.738,18.688 7.765,19.404C8.47,19.897 9.061,20.184 9.685,20.356C10.317,20.531 11.029,20.6 12,20.6C16.75,20.6 20.6,16.75 20.6,12C20.6,7.25 16.75,3.4 12,3.4C7.25,3.4 3.4,7.25 3.4,12C3.4,13.321 3.697,14.57 4.228,15.686L4.613,16.498L2.987,17.271L2.602,16.458C1.959,15.106 1.6,13.593 1.6,12Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.093,11.1H7.707V12.9H11.093V16.293H12.894V12.9H16.293V11.1H12.894V7.707H11.093V11.1Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val tilePictureAsk: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_tile_picture_ask",
            defaultWidth = 22.dp,
            defaultHeight = 22.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M13,5.15C14.574,5.15 15.85,6.426 15.85,8C15.85,9.574 14.574,10.85 13,10.85C11.426,10.85 10.15,9.574 10.15,8C10.15,6.426 11.426,5.15 13,5.15ZM13,6.85C12.365,6.85 11.85,7.365 11.85,8C11.85,8.635 12.365,9.15 13,9.15C13.635,9.15 14.15,8.635 14.15,8C14.15,7.365 13.635,6.85 13,6.85Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M12.6,2.15C13.706,2.15 14.604,2.149 15.321,2.227C16.055,2.306 16.699,2.476 17.263,2.886C17.59,3.123 17.877,3.41 18.114,3.737C18.524,4.301 18.694,4.945 18.773,5.679C18.851,6.396 18.85,7.294 18.85,8.4V11.6C18.85,12.706 18.851,13.604 18.773,14.321C18.694,15.055 18.524,15.699 18.114,16.263C17.877,16.59 17.59,16.877 17.263,17.114C16.699,17.524 16.055,17.694 15.321,17.773C14.604,17.851 13.706,17.85 12.6,17.85H7.4C6.294,17.85 5.396,17.851 4.679,17.773C3.945,17.694 3.301,17.524 2.737,17.114C2.41,16.877 2.123,16.59 1.886,16.263C1.476,15.699 1.306,15.055 1.227,14.321C1.149,13.604 1.15,12.706 1.15,11.6V8.4C1.15,7.294 1.149,6.396 1.227,5.679C1.306,4.945 1.476,4.301 1.886,3.737C2.123,3.41 2.41,3.123 2.737,2.886C3.301,2.476 3.945,2.306 4.679,2.227C5.396,2.149 6.294,2.15 7.4,2.15H12.6ZM5.563,10.816C5.31,10.835 5.025,10.929 4.58,11.197C4.138,11.464 3.61,11.856 2.854,12.423C2.858,13.141 2.869,13.69 2.917,14.138C2.981,14.726 3.097,15.038 3.261,15.264C3.393,15.446 3.554,15.607 3.736,15.739C3.962,15.903 4.274,16.019 4.862,16.083C5.467,16.149 6.257,16.15 7.4,16.15H11.949L8.711,12.913C8,12.203 7.511,11.715 7.096,11.376C6.693,11.047 6.424,10.914 6.175,10.86C5.974,10.817 5.768,10.802 5.563,10.816ZM7.4,3.85C6.257,3.85 5.467,3.852 4.862,3.917C4.274,3.981 3.962,4.097 3.736,4.261C3.554,4.393 3.393,4.554 3.261,4.736C3.097,4.962 2.981,5.274 2.917,5.862C2.852,6.467 2.85,7.257 2.85,8.4V10.311C3.157,10.09 3.439,9.899 3.702,9.74C4.266,9.4 4.816,9.166 5.441,9.121C5.808,9.095 6.176,9.121 6.535,9.199C7.148,9.332 7.659,9.642 8.17,10.059C8.668,10.465 9.227,11.025 9.913,11.711L14.333,16.132C14.636,16.122 14.901,16.109 15.138,16.083C15.726,16.019 16.038,15.903 16.264,15.739C16.446,15.607 16.607,15.446 16.739,15.264C16.903,15.038 17.019,14.726 17.083,14.138C17.149,13.533 17.15,12.743 17.15,11.6V8.4C17.15,7.257 17.149,6.467 17.083,5.862C17.019,5.274 16.903,4.962 16.739,4.736C16.607,4.554 16.446,4.393 16.264,4.261C16.038,4.097 15.726,3.981 15.138,3.917C14.533,3.852 13.743,3.85 12.6,3.85H7.4Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
    val trashOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_trash_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M16.953,6.642L16.673,12.252C16.563,14.452 16.517,15.349 15.78,16.377C15.475,16.802 15.093,17.166 14.653,17.449C14.09,17.812 13.459,17.967 12.728,18.04C12.01,18.111 11.115,18.11 9.999,18.11C8.884,18.11 7.99,18.111 7.272,18.04C6.541,17.967 5.91,17.812 5.347,17.449C4.907,17.166 4.524,16.802 4.22,16.377C3.483,15.349 3.437,14.452 3.327,12.252L3.047,6.642L4.626,6.563L4.907,12.174C5.023,14.493 5.09,14.876 5.505,15.455C5.694,15.719 5.93,15.945 6.203,16.12C6.471,16.293 6.822,16.405 7.429,16.466C8.05,16.528 8.852,16.529 9.999,16.529C11.147,16.529 11.949,16.528 12.57,16.466C13.178,16.405 13.529,16.293 13.797,16.12C14.069,15.945 14.306,15.719 14.495,15.455C14.91,14.876 14.977,14.493 15.093,12.174L15.374,6.563L16.953,6.642ZM7.204,8.053H8.785V13.607H7.204V8.053ZM11.214,8.053H12.796V13.607H11.214V8.053ZM10.57,1.89C11.251,1.89 11.766,1.881 12.25,2.039C12.401,2.088 12.548,2.148 12.689,2.22C13.143,2.452 13.501,2.822 13.982,3.303L14.81,4.131H17.844V5.713H2.155V4.131H5.189L6.017,3.303C6.499,2.822 6.857,2.452 7.311,2.22C7.452,2.148 7.599,2.088 7.75,2.039C8.234,1.881 8.749,1.89 9.43,1.89H10.57ZM9.43,3.471C8.648,3.471 8.43,3.481 8.239,3.542C8.167,3.566 8.096,3.595 8.029,3.629C7.886,3.702 7.756,3.811 7.429,4.131H12.57C12.244,3.811 12.113,3.702 11.971,3.629C11.903,3.595 11.833,3.566 11.761,3.542C11.57,3.481 11.352,3.471 10.57,3.471H9.43Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val typePdf: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_type_pdf",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M18.5,0L25.5,7V22.705C25.5,24.559 25.5,25.485 25.169,26.193C24.879,26.816 24.414,27.322 23.844,27.639C23.195,28 22.346,28 20.647,28H7.353C5.655,28 4.805,28 4.156,27.639C3.585,27.322 3.121,26.816 2.831,26.193C2.5,25.485 2.5,24.559 2.5,22.705V5.295C2.5,3.441 2.5,2.515 2.831,1.807C3.121,1.184 3.585,0.678 4.156,0.361C4.805,0 5.655,0 7.353,0H18.5Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.5,0L25.5,7H20.506C19.804,7 19.453,7 19.185,6.863C18.949,6.743 18.757,6.551 18.637,6.315C18.5,6.047 18.5,5.696 18.5,4.994V0Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.72,18.704C17.743,18.631 16.802,18.269 16.042,17.618C14.558,17.944 13.146,18.414 11.734,18.993C10.613,20.984 9.563,21.997 8.658,21.997C8.477,21.997 8.26,21.961 8.115,21.852C7.717,21.671 7.5,21.273 7.5,20.875C7.5,20.549 7.572,19.645 11.011,18.161C11.807,16.713 12.422,15.229 12.929,13.673C12.495,12.804 11.554,10.669 12.205,9.583C12.422,9.185 12.856,8.968 13.327,9.004C13.689,9.004 14.051,9.185 14.268,9.474C14.738,10.126 14.702,11.501 14.087,13.528C14.666,14.614 15.426,15.591 16.331,16.423C17.091,16.279 17.851,16.17 18.611,16.17C20.312,16.206 20.566,17.003 20.529,17.473C20.529,18.704 19.335,18.704 18.72,18.704ZM8.586,20.948L8.694,20.911C9.201,20.73 9.599,20.368 9.889,19.898C9.346,20.115 8.912,20.477 8.586,20.948ZM13.399,10.09H13.291C13.255,10.09 13.182,10.09 13.146,10.126C13.001,10.741 13.11,11.393 13.363,11.972C13.58,11.356 13.58,10.705 13.399,10.09ZM13.653,15.338L13.617,15.41L13.58,15.374C13.255,16.206 12.893,17.039 12.495,17.835L12.567,17.799V17.871C13.363,17.582 14.232,17.328 15.028,17.147L14.992,17.111H15.101C14.558,16.568 14.051,15.953 13.653,15.338ZM18.575,17.256C18.249,17.256 17.96,17.256 17.634,17.328C17.996,17.509 18.358,17.582 18.72,17.618C18.973,17.654 19.226,17.618 19.444,17.545C19.444,17.437 19.299,17.256 18.575,17.256Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val typePic: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_type_pic",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M2.831,1.807C2.5,2.515 2.5,3.441 2.5,5.295V22.705C2.5,24.559 2.5,25.485 2.831,26.193C3.121,26.816 3.585,27.322 4.156,27.639C4.805,28 5.655,28 7.353,28H20.647C22.346,28 23.195,28 23.844,27.639C24.414,27.322 24.879,26.816 25.169,26.193C25.5,25.485 25.5,24.559 25.5,22.705V7L18.5,0H7.353C5.655,0 4.805,0 4.156,0.361C3.585,0.678 3.121,1.184 2.831,1.807Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.5,0L25.5,7H20.506C19.804,7 19.453,7 19.185,6.863C18.949,6.743 18.757,6.551 18.637,6.315C18.5,6.047 18.5,5.696 18.5,4.994V0Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M10.655,16.592C10.809,16.328 11.191,16.328 11.345,16.592L14.149,21.399C14.305,21.665 14.112,22 13.804,22H8.196C7.888,22 7.695,21.665 7.851,21.399L10.655,16.592Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.15,13.629C16.303,13.355 16.697,13.355 16.85,13.629L21.17,21.406C21.318,21.672 21.125,22 20.82,22H12.18C11.875,22 11.682,21.672 11.83,21.406L16.15,13.629Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M9.3,11.8m-1.8,0a1.8,1.8 0,1 1,3.6 0a1.8,1.8 0,1 1,-3.6 0").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val typeText: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_type_text",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M2.831,1.807C2.5,2.515 2.5,3.441 2.5,5.295V22.705C2.5,24.559 2.5,25.485 2.831,26.193C3.121,26.816 3.585,27.322 4.156,27.639C4.805,28 5.655,28 7.353,28H20.647C22.346,28 23.195,28 23.844,27.639C24.414,27.322 24.879,26.816 25.169,26.193C25.5,25.485 25.5,24.559 25.5,22.705V7L18.5,0H7.353C5.655,0 4.805,0 4.156,0.361C3.585,0.678 3.121,1.184 2.831,1.807Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.5,0L25.5,7H20.506C19.804,7 19.453,7 19.185,6.863C18.949,6.743 18.757,6.551 18.637,6.315C18.5,6.047 18.5,5.696 18.5,4.994V0Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.25,11.784C8.25,11.351 8.601,11 9.034,11H18.966C19.399,11 19.75,11.351 19.75,11.784C19.75,12.217 19.399,12.568 18.966,12.568H9.034C8.601,12.568 8.25,12.217 8.25,11.784Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.25,15.852C8.25,15.419 8.601,15.068 9.034,15.068H18.966C19.399,15.068 19.75,15.419 19.75,15.852C19.75,16.285 19.399,16.636 18.966,16.636H9.034C8.601,16.636 8.25,16.285 8.25,15.852Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.25,19.92C8.25,19.487 8.601,19.136 9.034,19.136H15.352C15.785,19.136 16.136,19.487 16.136,19.92C16.136,20.353 15.785,20.704 15.352,20.704H9.034C8.601,20.704 8.25,20.353 8.25,19.92Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val typeXls: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_type_xls",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M18.5,0L25.5,7V22.705C25.5,24.559 25.5,25.485 25.169,26.193C24.879,26.816 24.414,27.322 23.844,27.639C23.195,28 22.346,28 20.647,28H7.353C5.655,28 4.805,28 4.156,27.639C3.585,27.322 3.121,26.816 2.831,26.193C2.5,25.485 2.5,24.559 2.5,22.705V5.295C2.5,3.441 2.5,2.515 2.831,1.807C3.121,1.184 3.585,0.678 4.156,0.361C4.805,0 5.655,0 7.353,0H18.5Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M18.5,0L25.5,7H20.506C19.804,7 19.453,7 19.185,6.863C18.949,6.743 18.757,6.551 18.637,6.315C18.5,6.047 18.5,5.696 18.5,4.994V0Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M10.203,11C10.109,11 10.018,11.029 9.941,11.083C9.736,11.228 9.687,11.512 9.832,11.717L12.818,15.941L9.583,20.533C9.529,20.609 9.5,20.701 9.5,20.795C9.5,21.046 9.704,21.249 9.955,21.249H11.205C11.354,21.249 11.494,21.176 11.579,21.052L13.994,17.545L16.422,21.053C16.507,21.176 16.646,21.249 16.796,21.249H18.045C18.14,21.249 18.232,21.22 18.309,21.165C18.513,21.02 18.561,20.736 18.416,20.531L15.157,15.941L18.167,11.719C18.222,11.642 18.251,11.549 18.251,11.455C18.251,11.204 18.048,11 17.797,11H16.547C16.398,11 16.258,11.073 16.173,11.196L13.995,14.337L11.827,11.196C11.743,11.073 11.603,11 11.453,11H10.203Z").toNodes(),
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val undoOutline24: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_undo_outline_24",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M10.526,4.967L6.546,8.182H14.1C17.461,8.182 20.186,10.907 20.187,14.269C20.186,17.63 17.461,20.355 14.1,20.355H12.186V18.556H14.1C16.467,18.555 18.386,16.636 18.387,14.269C18.386,11.901 16.467,9.983 14.1,9.982H6.533L10.526,13.206L9.458,14.529L5.271,11.148C4.934,10.876 4.623,10.626 4.396,10.394C4.159,10.153 3.92,9.841 3.843,9.415C3.804,9.198 3.804,8.976 3.843,8.759C3.92,8.333 4.159,8.021 4.396,7.779C4.623,7.547 4.934,7.297 5.271,7.024L9.458,3.645L10.526,4.967Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val unpinOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_unpin_outline_20",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M6.826,9.162C6.818,9.163 6.81,9.167 6.802,9.169L4.091,9.619L10.379,15.906L10.831,13.197L10.837,13.172L12.225,14.56L11.894,16.553C11.706,17.676 10.328,18.118 9.523,17.313L2.687,10.476C1.882,9.671 2.323,8.292 3.447,8.105L5.438,7.773L6.826,9.162ZM10.327,2.31C10.61,2.225 10.917,2.302 11.125,2.51L17.49,8.874C17.698,9.082 17.775,9.389 17.69,9.672C17.604,9.954 17.37,10.166 17.08,10.224L15.55,10.53L13.852,12.226L12.721,11.095L14.59,9.227C14.702,9.116 14.844,9.039 14.999,9.008L15.3,8.948L11.05,4.698L10.991,5.001C10.96,5.155 10.883,5.297 10.772,5.409L8.903,7.277L7.771,6.145L9.469,4.449L9.776,2.919L9.804,2.813C9.887,2.574 10.081,2.385 10.327,2.31Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M2.506,16.938C3.33,15.828 4.154,14.718 4.978,13.608C5.451,14.077 5.924,14.547 6.397,15.017C5.292,15.848 4.187,16.679 3.083,17.51C3.025,17.554 2.966,17.598 2.908,17.642C2.731,17.466 2.554,17.289 2.376,17.113C2.42,17.055 2.463,16.996 2.506,16.938Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M16.888,16.112L15.757,17.243L2.757,4.243L3.888,3.112L16.888,16.112Z").toNodes(),
                fill = SolidColor(Color(0xFF0F0F0F)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val userRegular: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_user_regular",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M15.135,8.553C15.135,6.821 13.731,5.418 12,5.418C10.269,5.418 8.865,6.821 8.865,8.553C8.865,10.284 10.269,11.687 12,11.688C13.731,11.688 15.135,10.284 15.135,8.553ZM16.635,8.553C16.635,11.113 14.56,13.188 12,13.188C9.44,13.187 7.365,11.113 7.365,8.553C7.365,5.993 9.44,3.918 12,3.918C14.56,3.918 16.635,5.993 16.635,8.553Z").toNodes(),
                fill = SolidColor(Color(0xFF0F1115)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M12,14.247C13.989,14.247 15.735,14.69 17.072,15.597C18.424,16.515 19.306,17.877 19.588,19.587L18.009,19.847C17.797,18.562 17.157,17.589 16.173,16.921C15.172,16.242 13.766,15.847 12,15.847C10.235,15.847 8.829,16.243 7.828,16.923C6.844,17.592 6.203,18.566 5.99,19.848L4.412,19.586C4.695,17.879 5.577,16.519 6.929,15.6C8.265,14.692 10.011,14.247 12,14.247Z").toNodes(),
                fill = SolidColor(Color(0xFF0F1115)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val voiceOutline26: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_voice_outline_26",
            defaultWidth = 26.dp,
            defaultHeight = 26.dp,
            viewportWidth = 26f,
            viewportHeight = 26f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M14.754,19.421C18.301,15.875 18.301,10.125 14.754,6.578").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.428,16.553C13.391,14.59 13.391,11.408 11.428,9.445").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 1.8f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M8.237,12.998m-0.8,0a0.8,0.8 0,1 1,1.6 0a0.8,0.8 0,1 1,-1.6 0").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 0.990804f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
            addPath(
                pathData = PathParser().parsePathString("M13.001,12.999m-11.331,0a11.331,11.331 0,1 1,22.662 0a11.331,11.331 0,1 1,-22.662 0").toNodes(),
                fill = null,
                stroke = SolidColor(Color(0xFF0F0F0F)),
                strokeLineWidth = 1.7f, strokeLineCap = StrokeCap.Butt, strokeLineJoin = StrokeJoin.Miter,
            )
        }.build()
    }
    val wand: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_wand",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M6.374,5.017L4.897,4.499C4.65,4.413 4.413,4.65 4.499,4.897L5.017,6.374C5.366,7.366 5.191,8.467 4.554,9.303L3.604,10.547C3.446,10.755 3.598,11.055 3.86,11.049L5.424,11.012C6.476,10.988 7.469,11.493 8.067,12.358L8.957,13.645C9.106,13.861 9.438,13.808 9.513,13.557L9.961,12.058C10.263,11.051 11.051,10.263 12.058,9.961L13.557,9.513C13.808,9.438 13.861,9.106 13.645,8.957L12.358,8.067C11.493,7.469 10.987,6.476 11.012,5.424L11.049,3.86C11.055,3.598 10.755,3.446 10.547,3.604L9.303,4.554C8.467,5.191 7.366,5.366 6.374,5.017ZM4.801,2.81C3.565,2.377 2.377,3.565 2.81,4.801L3.544,6.891C3.718,7.387 3.631,7.938 3.312,8.356L1.968,10.117C1.174,11.158 1.937,12.656 3.247,12.625L5.461,12.573C5.987,12.561 6.483,12.814 6.782,13.247L8.042,15.068C8.787,16.146 10.447,15.883 10.823,14.628L11.458,12.506C11.608,12.002 12.002,11.608 12.506,11.458L14.628,10.823C15.883,10.447 16.146,8.787 15.068,8.042L13.247,6.782C12.814,6.483 12.561,5.987 12.573,5.461L12.625,3.247C12.656,1.937 11.158,1.174 10.116,1.968L8.356,3.312C7.938,3.631 7.387,3.718 6.891,3.544L4.801,2.81Z").toNodes(),
                fill = SolidColor(Color(0xFF000000)),
                pathFillType = PathFillType.EvenOdd,
            )
            addPath(
                pathData = PathParser().parsePathString("M12.387,13.729C12.082,13.348 12.111,12.799 12.456,12.454C12.801,12.109 13.351,12.08 13.731,12.385L17.115,15.107C17.777,15.639 17.83,16.628 17.23,17.229C16.629,17.83 15.639,17.776 15.107,17.114L12.387,13.729Z").toNodes(),
                fill = SolidColor(Color(0xFF191919)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val warningOutline16: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_warning_outline_16",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M7.2,3.804L8.8,3.804L8.8,8.911H7.2L7.2,3.804Z").toNodes(),
                fill = SolidColor(Color(0xFFF59E0B)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M7.2,10.308H8.8V12.196H7.2V10.308Z").toNodes(),
                fill = SolidColor(Color(0xFFF59E0B)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M14.438,8C14.438,4.445 11.555,1.563 8,1.563C4.445,1.563 1.563,4.445 1.563,8C1.563,11.555 4.445,14.438 8,14.438C11.556,14.438 14.438,11.556 14.438,8ZM15.838,8C15.838,12.329 12.329,15.838 8,15.838C3.671,15.838 0.162,12.329 0.162,8C0.162,3.671 3.671,0.162 8,0.162C12.329,0.162 15.838,3.671 15.838,8Z").toNodes(),
                fill = SolidColor(Color(0xFFF59E0B)),
                pathFillType = PathFillType.NonZero,
            )
        }.build()
    }
    val warningOutline20: ImageVector by lazy {
        ImageVector.Builder(
            name = "ic_warning_outline_20",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathParser().parsePathString("M11.028,6.966L12.948,6.966L12.948,13.093L11.028,13.093L11.028,6.966Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M11.028,14.77L12.948,14.77L12.948,17.036L11.028,17.036L11.028,14.77Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.NonZero,
            )
            addPath(
                pathData = PathParser().parsePathString("M12,21.525C6.739,21.525 2.475,17.261 2.475,12C2.475,6.739 6.739,2.475 12,2.475C17.261,2.475 21.525,6.739 21.525,12C21.525,17.261 17.261,21.525 12,21.525ZM12,19.605C16.2,19.605 19.605,16.2 19.605,12C19.605,7.8 16.2,4.395 12,4.395C7.8,4.395 4.395,7.8 4.395,12C4.395,16.2 7.8,19.605 12,19.605Z").toNodes(),
                fill = SolidColor(Color(0xFF7D7F85)),
                pathFillType = PathFillType.EvenOdd,
            )
        }.build()
    }
}
