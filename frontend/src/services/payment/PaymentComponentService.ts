import React from 'react';
import { PaymentNextAction } from '@/interfaces/payment.interface';
import { paymentNextActionService, NextActionComponentProps } from './PaymentNextActionService';

export interface PaymentComponentConfig {
  component: React.ComponentType<Record<string, unknown>>;
  props: Record<string, unknown>;
  isSupported: boolean;
  errors?: string[];
}

export class PaymentComponentService {
  /**
   * Get complete component configuration for a payment next action
   */
  getComponentConfig(
    paymentNextAction: PaymentNextAction,
    additionalProps: NextActionComponentProps = {}
  ): PaymentComponentConfig {
    // Validate the next action
    const validation = paymentNextActionService.validateNextAction(paymentNextAction);
    
    if (!validation.isValid) {
      return {
        component: this.getErrorComponent() as React.ComponentType<Record<string, unknown>>,
        props: { errors: validation.errors },
        isSupported: false,
        errors: validation.errors
      };
    }

    // Get the component for this next action type
    const component = paymentNextActionService.getNextActionComponent(paymentNextAction);
    
    if (!component) {
      return {
        component: this.getUnavailableComponent() as React.ComponentType<Record<string, unknown>>,
        props: { type: paymentNextAction.type },
        isSupported: false,
        errors: [`Component not available for type: ${paymentNextAction.type}`]
      };
    }

    // Get the props for this component
    const componentProps = paymentNextActionService.getNextActionProps(
      paymentNextAction, 
      additionalProps
    );

    return {
      component,
      props: componentProps,
      isSupported: true
    };
  }

  /**
   * Render a payment next action component
   */
  renderComponent(
    paymentNextAction: PaymentNextAction,
    additionalProps: NextActionComponentProps = {}
  ): React.ReactElement {
    const config = this.getComponentConfig(paymentNextAction, additionalProps);
    return React.createElement(config.component, config.props);
  }

  /**
   * Check if a payment next action can be rendered
   */
  canRender(paymentNextAction: PaymentNextAction): boolean {
    const config = this.getComponentConfig(paymentNextAction);
    return config.isSupported;
  }

  /**
   * Get error component for unsupported payment methods
   */
  private getErrorComponent(): React.ComponentType<{ errors: string[] }> {
    const ErrorComponent = ({ errors }: { errors: string[] }) => React.createElement(
      'div',
      {
        className: 'flex flex-col items-center space-y-4 p-6 bg-red-50 rounded-lg border border-red-200'
      },
      React.createElement(
        'div',
        { className: 'text-center' },
        React.createElement(
          'h3',
          { className: 'text-lg font-semibold text-red-800' },
          'Payment Error'
        ),
        React.createElement(
          'div',
          { className: 'text-sm text-red-600 mt-1' },
          errors.map((error, index) => 
            React.createElement('p', { key: index }, error)
          )
        )
      )
    );
    ErrorComponent.displayName = 'PaymentErrorComponent';
    return ErrorComponent;
  }

  /**
   * Get success component for completed payments
   */
  private getSuccessComponent(): React.ComponentType<{ message: string }> {
    const SuccessComponent = ({ message }: { message: string }) => React.createElement(
      'div',
      {
        className: 'flex flex-col items-center space-y-4 p-6 bg-green-50 rounded-lg border border-green-200'
      },
      React.createElement(
        'div',
        { className: 'text-center' },
        React.createElement(
          'h3',
          { className: 'text-lg font-semibold text-green-800' },
          'Payment Completed'
        ),
        React.createElement(
          'p',
          { className: 'text-sm text-green-600 mt-1' },
          message
        )
      )
    );
    SuccessComponent.displayName = 'PaymentSuccessComponent';
    return SuccessComponent;
  }

  /**
   * Get unavailable component for unsupported types
   */
  private getUnavailableComponent(): React.ComponentType<{ type: string }> {
    const UnavailableComponent = ({ type }: { type: string }) => React.createElement(
      'div',
      {
        className: 'flex flex-col items-center space-y-4 p-6 bg-yellow-50 rounded-lg border border-yellow-200'
      },
      React.createElement(
        'div',
        { className: 'text-center' },
        React.createElement(
          'h3',
          { className: 'text-lg font-semibold text-yellow-800' },
          'Component Not Available'
        ),
        React.createElement(
          'p',
          { className: 'text-sm text-yellow-600 mt-1' },
          `The component for "${type}" is not available.`
        )
      )
    );
    UnavailableComponent.displayName = 'PaymentUnavailableComponent';
    return UnavailableComponent;
  }
}

export const paymentComponentService = new PaymentComponentService();
