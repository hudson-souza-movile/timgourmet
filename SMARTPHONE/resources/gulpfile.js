var gulp = require('gulp');
var imagemin = require('gulp-tinypng');

var tinypng = require('gulp-tinypng-compress');

gulp.task('tinypng', function () {
	gulp.src('img/*.{png,jpg,jpeg}')
		.pipe(tinypng({
			key: 'JHpf7YZZq7SLD6pPssKd0zFD-5q4aeLw',
			sigFile: 'images/.tinypng-sigs',
			log: true
		}))
		.pipe(gulp.dest('img'));
});

// Default Task
gulp.task('default', ['tinypng']);