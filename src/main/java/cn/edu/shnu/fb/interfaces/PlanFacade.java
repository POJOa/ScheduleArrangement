package cn.edu.shnu.fb.interfaces;

/**
 * Created by bytenoob on 15/10/29.
 */

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cn.edu.shnu.fb.domain.imp.ImpCourse;
import cn.edu.shnu.fb.domain.imp.ImpCourseRepository;
import cn.edu.shnu.fb.domain.imp.ImpRepository;
import cn.edu.shnu.fb.domain.major.Major;
import cn.edu.shnu.fb.domain.major.MajorRepository;
import cn.edu.shnu.fb.domain.plan.Plan;
import cn.edu.shnu.fb.domain.plan.PlanCourse;
import cn.edu.shnu.fb.domain.plan.PlanCourseRepository;
import cn.edu.shnu.fb.domain.plan.PlanRepository;
import cn.edu.shnu.fb.domain.plan.PlanSpec;
import cn.edu.shnu.fb.domain.plan.PlanSpecRepository;

@RequestMapping("/api")
@RestController

public class PlanFacade {

    @Autowired
    PlanRepository planRepository;

    @Autowired
    PlanCourseRepository planCourseRepository;

    @Autowired
    PlanSpecRepository planSpecRepository;

    @Autowired
    MajorRepository majorRepository;

    @Autowired
    ImpCourseRepository impCourseRepository;
    @ResponseBody
    @RequestMapping(value="/plan/{id}",method=RequestMethod.GET)
    public List<PlanDTO> planBrief(@PathVariable Integer id,@PathVariable Integer detailed){
        Iterable<Plan> plans = planRepository.findAll();
        List<PlanDTO> planDTOs = new ArrayList<PlanDTO>();
        Boolean shouldLoadDetailedPlan = false;
        for(Plan planTmp : plans){
            PlanDTO planDTO = new PlanDTO(planTmp,shouldLoadDetailedPlan);
            planDTOs.add(planDTO);
        }
        return planDTOs;
    }

    @ResponseBody
    @RequestMapping(value="/plan/major/{grade}/{title}/term/{termCount}",method=RequestMethod.GET)
    public List<PlanCourseGridDTO> planBriefByTermAndMajor(@PathVariable Integer grade,@PathVariable String title,@PathVariable Integer termCount){
        Major major = majorRepository.findMajorByGradeAndTitleLike(grade, title);
        Boolean shouldLoadDetailedPlan = true;
        int termYear = grade + (termCount - 1) / 2 ;
        int termPart = termCount % 2;
        Iterable<Plan> plans = planRepository.findPlansByMajorAndTermYearAndTermPart(major, termYear, termPart);
        List<PlanCourseGridDTO> PCGDTOs = new ArrayList<>();
        List<PlanCourse> PCDTOs = new ArrayList<>();

        for(Plan planTmp : plans){
            PCDTOs = planTmp.getPlanCourses();
            for(PlanCourse PCDTO : PCDTOs) {
                PlanCourseGridDTO tmpPCGDTO = new PlanCourseGridDTO(new PlanCourseDTO(PCDTO));
                tmpPCGDTO.setOnePeriod(PCDTO.getPeriod());
                tmpPCGDTO.setOneCredits(PCDTO.getCredits());
                PCGDTOs.add(tmpPCGDTO);
            }
        }
        return PCGDTOs;
    }


    @ResponseBody
    @RequestMapping(value="/plan/spec/major/{grade}/{title}/term/{termCount}",method=RequestMethod.GET)
    public List<PlanCourseGridDTO> planSpecByTermAndMajor(@PathVariable Integer grade,@PathVariable String title,@PathVariable Integer termCount){
        Major major = majorRepository.findMajorByGradeAndTitleLike(grade, title);
        Boolean shouldLoadDetailedPlan = true;
        int termYear = grade + (termCount - 1) / 2 ;
        int termPart = termCount % 2;
        Iterable<Plan> plans = planRepository.findPlansByMajorAndTermYearAndTermPart(major,termYear,termPart);
        List<PlanCourseGridDTO> PCGDTOs = new ArrayList<>();
        List<PlanSpec> PSDTOs = new ArrayList<>();

        for(Plan planTmp : plans){
            PSDTOs = planTmp.getPlanSpecs();
            for(PlanSpec PSDTO : PSDTOs) {
                PlanCourseGridDTO tmpPCGDTO = new PlanCourseGridDTO(new PlanSpecDTO(PSDTO));
                tmpPCGDTO.setOnePeriod(PSDTO.getPeriod());
                tmpPCGDTO.setOneCredits(PSDTO.getCredits());
                PCGDTOs.add(tmpPCGDTO);
            }
        }
        return PCGDTOs;
    }

    @ResponseBody
    @RequestMapping(value="/plan/spec/major/{grade}/{title}/term/{termCount}/detail",method=RequestMethod.GET)
    public List<PlanSpec> planSpecDetailByTermAndMajor(@PathVariable Integer grade,@PathVariable String title,@PathVariable Integer termCount){
        Major major = majorRepository.findMajorByGradeAndTitleLike(grade, title);
        int termYear = grade + (termCount - 1) / 2 ;
        int termPart = termCount % 2;
        Iterable<Plan> plans = planRepository.findPlansByMajorAndTermYearAndTermPart(major,termYear,termPart);
        List<PlanCourseGridDTO> PCGDTOs = new ArrayList<>();
        List<PlanSpec> PSDTOs = new ArrayList<>();

        for(Plan planTmp : plans){
            PSDTOs = planTmp.getPlanSpecs();
        }
        return PSDTOs;
    }

    @ResponseBody
    @RequestMapping(value="/plan/detailed/major/{grade}/{title}",method=RequestMethod.GET)
    public List<PlanCourseGridDTO> detailedPlanByMajor(@PathVariable Integer grade,@PathVariable String title){
        Major major = majorRepository.findMajorByGradeAndTitleLike(grade, title);
        if(major != null) {
            Iterable<Plan> plans = planRepository.findPlansByMajor(major);
            List<PlanCourse> planCourses = new ArrayList();
            List<PlanSpec> planSpecs = new ArrayList();

            for (Plan planTmp : plans) {
                if(planTmp.getTerm()!=null) {
                    planCourses.addAll(planCourseRepository.findPlanCoursesByPlan(planTmp));
                    planSpecs.addAll(planSpecRepository.findPlanSpecsByPlan(planTmp));
                }
            }
            List<PlanCourseDTO> planCourseDTOs = new ArrayList();
            List<PlanSpecDTO> planSpecDTOs = new ArrayList();

            for (PlanCourse planCourseTmp : planCourses) {
                PlanCourseDTO planCourseDTO = new PlanCourseDTO(planCourseTmp);
                planCourseDTOs.add(planCourseDTO);
            }

            for (PlanSpec planSpecTmp : planSpecs) {
                PlanSpecDTO planSpecDTO = new PlanSpecDTO(planSpecTmp);
                planSpecDTOs.add(planSpecDTO);
            }

            List<PlanCourseGridDTO> PCGDTO = PlanAssembler.mergePlanCoursesAndCreateGridDTO(planCourseDTOs, major);
            List<PlanCourseGridDTO> PCGDTO2 = PlanAssembler.mergePlanSpecsAndCreateGridDTO(planSpecDTOs, major);
            PCGDTO.addAll(PCGDTO2);
            return PCGDTO;
        }
        return null;
    }

    @ResponseBody
    @RequestMapping(value="/plan/class/{courseClass}/type/{courseType}/major/{grade}/{title}",method=RequestMethod.GET)
    public List<PlanCourseGridDTO> planSelectableCourseByMajor(@PathVariable Integer courseClass,@PathVariable Integer courseType,@PathVariable Integer grade,@PathVariable String title){
        Major major = majorRepository.findMajorByGradeAndTitleLike(grade, title);
        if(major != null) {
            Iterable<Plan> plans = planRepository.findPlansByMajor(major);
            Plan genericPlan = null;
            List<PlanCourse> planCourses = new ArrayList();
            List<PlanCourseGridDTO> PCGDTOs = new ArrayList<>();
            for (Plan planTmp : plans) {
                if(planTmp.getTerm() == null) {
                    genericPlan = planTmp;
                }
            }
            Boolean isImped = false;
            List<ImpCourse> impCourses = impCourseRepository.findImpCoursesByImpMajor(major);
            planCourses = planCourseRepository.findPlanCoursesByPlan(genericPlan);
            if(genericPlan!=null) {
                for (PlanCourse planCourseTmp : planCourses) {
                    if(planCourseTmp.getCourseClass().getId() == courseClass) {
                        if (courseType == 0 || planCourseTmp.getCourseType() != null && planCourseTmp.getCourseType().getId() == courseType) {
                            for(ImpCourse impCourse : impCourses) {
                                if (impCourse.getCourse().getId() == planCourseTmp.getCourse().getId()) {
                                    isImped = true;
                                    break;
                                }
                            }
                            if(!isImped) {
                                PlanCourseDTO planCourseDTO = new PlanCourseDTO(planCourseTmp);
                                PlanCourseGridDTO PCGDTO = new PlanCourseGridDTO(planCourseDTO);
                                PCGDTO.setOneCredits(planCourseDTO.getCredits());
                                PCGDTO.setOnePeriod(planCourseDTO.getPeriod());
                                PCGDTOs.add(PCGDTO);
                            }
                            isImped = false;
                        }
                    }
                }
            }
            return PCGDTOs;
        }
        return null;
    }
}