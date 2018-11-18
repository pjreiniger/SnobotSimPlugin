
#pragma once

#ifdef _MSC_VER
#define EXPORT_ __declspec(dllexport)
#else
#define EXPORT_
#endif

#include <memory>


template <typename RobotClass>
class EXPORT_ SimulatorJniWrapper
{
public:
    SimulatorJniWrapper(std::shared_ptr<RobotClass> robotClass) :
        mRobot(robotClass)
    {
    
    }
    virtual ~SimulatorJniWrapper()
    {
    
    }

    std::shared_ptr<RobotClass> GetRobot()
    {
        return mRobot;
    }
protected:

    std::shared_ptr<RobotClass> mRobot;
};
