'use strict';

angular.module('capApp')



angular.module('capApp')
.controller('PnlPreviewModalInstanceCtrl', function ($rootScope, $scope, $uibModalInstance, $uibModal, $log, $translate, pnlPreviewService, getResolvedTemplate, $window) {
	
	
	var resolvedTemplate = getResolvedTemplate;

	$scope.measureUnit = "mm";
	if($rootScope.unitSystem == "US")$scope.measureUnit = "inches";	
	
	$scope.pageWidth = 0;
	$scope.pageHeight = 0;
	$scope.selectedFormat = 'a4';
	$scope.dimensionsChoice = false;
	
	$scope.pageSizes = ['a0','a1','a2','a3','a4','a5','a6','a7','a8','a9','a10','2a0','4a0',
		'b0','b1','b1+','b2','b2+','b3','b4','b5','b6','b7','b8','b9','b10','b11','b12',
		'c0','c1','c2','c3','c4','c5','c6','c7','c8','c9','c10','c11','c12'];
	
	var inchesToMm = function (value) {
		
		var converted = value * 25.4;
		var rounded = Math.round( converted * 10 ) / 10;
		return rounded;
		
	};
	
	$scope.createAndDisplayPdf = function () {
		
		var previewDimensions = null;	
		
		
		if(!$scope.dimensionsChoice && $scope.selectedFormat){

			var dimensionsArray = pagesDimensions[$scope.selectedFormat];
			previewDimensions = { width: dimensionsArray[0], height: dimensionsArray[1] };
			
			
		}

		if($scope.dimensionsChoice && $scope.pageWidth  && $scope.pageHeight){
			//if unit is inches ==> convert to mm
			var finalWidth = $scope.pageWidth;
			var finalHeight = $scope.pageHeight;
			if($scope.measureUnit == "inches"){
				finalWidth = inchesToMm($scope.pageWidth);
				finalHeight = inchesToMm($scope.pageHeight);
			}
			previewDimensions = { width: finalWidth, height: finalHeight };
		}

		
		//server side rendering
		var pnlData = {
			pageSize: previewDimensions,
			template: resolvedTemplate, 
		};
		
		resolvedTemplate.pageWidth  = previewDimensions.width;
		resolvedTemplate.pageHeight  = previewDimensions.height;
		
		pnlPreviewService.generatePdfPreview(resolvedTemplate).then(function (base64EncodedPDF) {			
			var pdfData = "data:application/pdf;base64," +base64EncodedPDF;
			$window.open(pdfData, '_blank');
        });
        
		//------------------------------------------------------
        

	   	
	   	
	   	
	   	
	   	
	     
	 };
	

	 
	$scope.closeModal = function(){
		$uibModalInstance.dismiss();
	};
	
	
	var pagesDimensions = {
			'a0':[ 841, 1189 ],
			'a1':[ 594, 841 ],
			'a2':[ 420, 594 ],
			'a3':[ 297, 420 ],
			'a4':[ 210, 297 ],
			'a5':[ 148, 210 ],
			'a6':[ 105, 148 ],
			'a7':[ 74, 105 ],
			'a8':[ 52, 74 ],
			'a9':[ 37, 52 ],
			'a10':[ 26, 37 ],
			'2a0':[ 1189, 1682 ],
			'4a0':[ 1682, 2378 ],
			'b0':[ 1000, 1414 ],
			'b1':[ 707, 1000 ],
			'b1+':[ 720, 1020 ],
			'b2':[ 500, 707 ],
			'b2+':[ 520, 720 ],
			'b3':[ 353, 500 ],
			'b4':[ 250, 353 ],
			'b5':[ 176, 250 ],
			'b6':[ 125, 176 ],
			'b7':[ 88, 125 ],
			'b8':[ 62, 88 ],
			'b9':[ 44, 62 ],
			'b10':[ 31, 44 ],
			'b11':[ 22, 32 ],
			'b12':[ 16, 22 ],
			'c0':[ 917, 1297 ],
			'c1':[ 648, 917 ],
			'c2':[ 458, 648 ],
			'c3':[ 324, 458 ],
			'c4':[ 229, 324 ],
			'c5':[ 162, 229 ],
			'c6':[ 114, 162 ],
			'c7':[ 81, 114 ],
			'c8':[ 57, 81 ],
			'c9':[ 40, 57 ],
			'c10':[ 28, 40 ],
			'c11':[ 22, 32 ],
			'c12':[ 16, 22 ]
	};
	
	
	
	  
});



